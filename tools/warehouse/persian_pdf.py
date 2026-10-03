# -*- coding: utf-8 -*-
"""
persian_pdf.py — dependency-free, right-to-left Persian PDF writer.

Exists because the sandbox has no Java/Android/PDF toolchain, so the Atiran
Warehouse engineering report (Persian, RTL, Vazirmatn) is produced in pure
Python. It embeds Vazirmatn TrueType files as CIDFontType2 (Identity-H),
performs Arabic contextual shaping + bidi visual reordering, and draws pages,
headings, paragraphs, bullets, tables, LTR code blocks and JPEG images.
"""
import struct, zlib

class TTF:
    def __init__(self, path):
        d = open(path, 'rb').read()
        self.data = d
        num = struct.unpack('>H', d[4:6])[0]
        off = 12
        self.tables = {}
        for _ in range(num):
            tag, _, toff, tlen = struct.unpack('>4sIII', d[off:off+16])
            self.tables[tag.decode()] = (toff, tlen)
            off += 16
        self._parse()

    def _u16(self, o): return struct.unpack('>H', self.data[o:o+2])[0]
    def _u32(self, o): return struct.unpack('>I', self.data[o:o+4])[0]

    def _parse(self):
        ho, _ = self.tables['head']; self.upem = self._u16(ho + 18)
        mo, _ = self.tables['maxp']; self.nglyphs = self._u16(mo + 4)
        he, _ = self.tables['hhea']; self.nhmtx = self._u16(he + 34)
        self._parse_hmtx(); self._parse_cmap()

    def _parse_hmtx(self):
        o, _ = self.tables['hmtx']; adv = []
        for i in range(self.nhmtx): adv.append(self._u16(o + i*4))
        last = adv[-1]
        for i in range(self.nhmtx, self.nglyphs): adv.append(last)
        self.adv = adv

    def _parse_cmap(self):
        o, _ = self.tables['cmap']; d = self.data
        n = self._u16(o + 2); best = None
        for i in range(n):
            pid, eid, soff = struct.unpack_from('>HHI', d, o + 4 + i*8)
            score = (pid == 3 and eid == 1) * 2 + (pid == 0)
            if best is None or score > best[0]: best = (score, soff)
        sub = o + best[1]; self.cmap = {}
        fmt = self._u16(sub)
        if fmt == 4:
            seg = self._u16(sub + 6) // 2
            end = [self._u16(sub + 14 + 2*i) for i in range(seg)]
            start = [self._u16(sub + 16 + 2*seg + 2*i) for i in range(seg)]
            delta = [struct.unpack('>h', d[sub+16+4*seg+2*i:sub+18+4*seg+2*i])[0] for i in range(seg)]
            roff = [self._u16(sub + 16 + 6*seg + 2*i) for i in range(seg)]
            for s in range(seg):
                for c in range(start[s], end[s] + 1):
                    if roff[s] == 0: g = (c + delta[s]) & 0xFFFF
                    else:
                        addr = sub + 16 + 6*seg + 2*s + roff[s] + 2*(c - start[s])
                        g = self._u16(addr)
                        if g: g = (g + delta[s]) & 0xFFFF
                    if g: self.cmap[c] = g
        elif fmt == 12:
            ng = self._u32(sub + 12)
            for i in range(ng):
                sc, ec, sg = struct.unpack_from('>III', d, sub + 16 + i*12)
                for c in range(sc, ec + 1): self.cmap[c] = sg + (c - sc)

    def gid(self, ch): return self.cmap.get(ord(ch), 0)
    def w(self, ch): return self.adv[self.gid(ch)] * 1000.0 / self.upem

R_JOIN = {0x622,0x623,0x624,0x625,0x627,0x629,0x62F,0x630,0x631,0x632,0x648,0x649,0x698,0x671}
D_JOIN = {0x626,0x628,0x62A,0x62B,0x62C,0x62D,0x62E,0x633,0x634,0x635,0x636,0x637,0x638,0x639,0x63A,0x640,
          0x641,0x642,0x643,0x644,0x645,0x646,0x647,0x64A,0x67E,0x686,0x6A9,0x6AF,0x6CC}
def is_ar(ch):
    o = ord(ch); return 0x600 <= o <= 0x6FF or 0xFB50 <= o <= 0xFEFF
def is_join(ch):
    o = ord(ch); return is_ar(ch) and (o in R_JOIN or o in D_JOIN)
def joins_left(ch):
    o = ord(ch); return is_ar(ch) and o in D_JOIN

F4 = {0x626:(0xFE89,0xFE8A,0xFE8B,0xFE8C),0x627:(0xFE8D,0xFE8E,None,None),0x628:(0xFE8F,0xFE90,0xFE91,0xFE92),
 0x629:(0xFE93,0xFE94,None,None),0x62A:(0xFE95,0xFE96,0xFE97,0xFE98),0x62B:(0xFE99,0xFE9A,0xFE9B,0xFE9C),
 0x62C:(0xFE9D,0xFE9E,0xFE9F,0xFEA0),0x62D:(0xFEA1,0xFEA2,0xFEA3,0xFEA4),0x62E:(0xFEA5,0xFEA6,0xFEA7,0xFEA8),
 0x62F:(0xFEA9,0xFEAA,None,None),0x630:(0xFEAB,0xFEAC,None,None),0x631:(0xFEAD,0xFEAE,None,None),0x632:(0xFEAF,0xFEB0,None,None),
 0x633:(0xFEB1,0xFEB2,0xFEB3,0xFEB4),0x634:(0xFEB5,0xFEB6,0xFEB7,0xFEB8),0x635:(0xFEB9,0xFEBA,0xFEBB,0xFEBC),
 0x636:(0xFEBD,0xFEBE,0xFEBF,0xFEC0),0x637:(0xFEC1,0xFEC2,0xFEC3,0xFEC4),0x638:(0xFEC5,0xFEC6,0xFEC7,0xFEC8),
 0x639:(0xFEC9,0xFECA,0xFECB,0xFECC),0x63A:(0xFECD,0xFECE,0xFECF,0xFED0),0x641:(0xFED1,0xFED2,0xFED3,0xFED4),
 0x642:(0xFED5,0xFED6,0xFED7,0xFED8),0x643:(0xFED9,0xFEDA,0xFEDB,0xFEDC),0x644:(0xFEDD,0xFEDE,0xFEDF,0xFEE0),
 0x645:(0xFEE1,0xFEE2,0xFEE3,0xFEE4),0x646:(0xFEE5,0xFEE6,0xFEE7,0xFEE8),0x647:(0xFEE9,0xFEEA,0xFEEB,0xFEEC),
 0x648:(0xFEED,0xFEEE,None,None),0x649:(0xFEEF,0xFEF0,None,None),0x64A:(0xFEF1,0xFEF2,0xFEF3,0xFEF4),
 0x622:(0xFE81,0xFE82,None,None),0x623:(0xFE83,0xFE84,None,None),0x624:(0xFE85,0xFE86,None,None),0x625:(0xFE87,0xFE88,None,None),
 0x67E:(0xFB56,0xFB57,0xFB58,0xFB59),0x686:(0xFB7A,0xFB7B,0xFB7C,0xFB7D),0x698:(0xFB8A,0xFB8B,None,None),
 0x6A9:(0xFB8E,0xFB8F,0xFB90,0xFB91),0x6AF:(0xFB92,0xFB93,0xFB94,0xFB95),0x6CC:(0xFBFC,0xFBFD,0xFBFE,0xFBFF)}
LAM_ALEF = {0x622:(0xFEF5,0xFEF6),0x623:(0xFEF7,0xFEF8),0x625:(0xFEF9,0xFEFA),0x627:(0xFEFB,0xFEFC)}

def shape(word):
    out = []; n = len(word); i = 0
    while i < n:
        ch = word[i]; o = ord(ch)
        prev = word[i-1] if i > 0 else ''
        nxt = word[i+1] if i+1 < n else ''
        if o == 0x644 and nxt and ord(nxt) in LAM_ALEF:
            joined = is_join(prev) if prev else False
            iso, fin = LAM_ALEF[ord(nxt)]
            out.append(chr(fin if joined else iso)); i += 2; continue
        if o in F4:
            iso, fin, ini, med = F4[o]
            right = (is_join(prev) if prev else False)
            left = (is_join(nxt) if nxt else False) and (o in D_JOIN)
            if right and left and med: f = med
            elif right and fin: f = fin
            elif left and ini: f = ini
            else: f = iso
            out.append(chr(f))
        else:
            out.append(ch)
        i += 1
    return out

def visual(word):
    if any(is_ar(c) for c in word):
        return list(reversed(shape(word)))
    return list(word)

def is_ltr_line(s): return not any(is_ar(c) for c in s)

GOLD = (0.72, 0.55, 0.16)
INK = (0.13, 0.15, 0.18)

class PDF:
    def __init__(self, reg, bold, med):
        self.fonts = {'reg': TTF(reg), 'bold': TTF(bold), 'med': TTF(med)}
        self.pages = []; self.W, self.H = 595.28, 841.89; self.M = 56
        self._imgs = {}
        self.new_page()

    def new_page(self):
        self.cur = []; self.pages.append(self.cur); self.y = self.H - self.M

    def rect(self, x, y, w, h, rgb, stroke=None):
        r, g, b = rgb
        self.cur.append("%.3f %.3f %.3f rg %.2f %.2f %.2f %.2f re f" % (r, g, b, x, y, w, h))
        if stroke:
            sr, sg, sb = stroke
            self.cur.append("%.3f %.3f %.3f RG 0.8 w %.2f %.2f %.2f %.2f re S" % (sr, sg, sb, x, y, w, h))

    def line(self, x1, y1, x2, y2, rgb, w=0.8):
        r, g, b = rgb
        self.cur.append("%.3f %.3f %.3f RG %.2f w %.2f %.2f m %.2f %.2f l S" % (r, g, b, w, x1, y1, x2, y2))

    def _seq(self, s):
        if is_ltr_line(s): return list(s)
        seq = []
        for tok in s.split(' '):
            seq += visual(tok); seq.append(' ')
        if seq: seq.pop()
        return seq

    def text_width(self, s, fnt, size):
        f = self.fonts[fnt]
        return sum(f.w(c) for c in self._seq(s)) * size / 1000.0

    def draw_run(self, x, y, s, fnt, size, rgb):
        f = self.fonts[fnt]; r, g, b = rgb
        cx = x
        for ch in self._seq(s):
            if ch == ' ': cx += f.w(' ') * size / 1000.0; continue
            wpx = f.w(ch) * size / 1000.0
            self.cur.append("BT /F%s %.1f Tf %.3f %.3f %.3f rg %.2f %.2f Td <%04X> Tj ET" % (fnt[0], size, r, g, b, cx, y, f.gid(ch)))
            cx += wpx
        return cx

    def wrap_rtl(self, s, fnt, size, maxw):
        words = s.split(' '); lines = []; cur = []; wcur = 0.0
        sp = self.fonts[fnt].w(' ') * size / 1000.0
        for w in words:
            ww = self.text_width(w, fnt, size)
            need = ww + (sp if cur else 0)
            if wcur + need > maxw and cur:
                lines.append(' '.join(cur)); cur = [w]; wcur = ww
            else:
                cur.append(w); wcur += need
        if cur: lines.append(' '.join(cur))
        return lines

    def _ensure(self, h):
        if self.y - h < self.M: self.new_page()

    def para(self, s, fnt='reg', size=9.5, rgb=INK, lh=1.55, align='rtl', space_after=6, indent=0):
        maxw = self.W - 2*self.M - indent
        lines = self.wrap_rtl(s, fnt, size, maxw)
        for ln in lines:
            self._ensure(lh*size + space_after)
            w = self.text_width(ln, fnt, size)
            x = self.W - self.M - w if align == 'rtl' else (self.W - w)/2 if align == 'center' else self.M + indent
            self.draw_run(x, self.y, ln, fnt, size, rgb)
            self.y -= lh * size
        self.y -= space_after

    def heading(self, s, level=1):
        sizes = {0: 20, 1: 14, 2: 12, 3: 10.5}
        self._ensure(40); self.y -= 6
        col = GOLD if level <= 1 else (0.20, 0.25, 0.32)
        self.para(s, fnt='bold', size=sizes[level], rgb=col, lh=1.3, space_after=2)
        if level <= 1: self.line(self.M, self.y+2, self.W-self.M, self.y+2, GOLD, 1.2)
        self.y -= 8

    def bullet(self, s, fnt='reg', size=9.5, rgb=INK):
        maxw = self.W - 2*self.M - 14
        lines = self.wrap_rtl(s, fnt, size, maxw); lh = 1.5*size
        self._ensure(lh*len(lines)+4)
        self.rect(self.W-self.M-3, self.y+3.4, 3, 3, GOLD)
        for ln in lines:
            self._ensure(lh)
            w = self.text_width(ln, fnt, size)
            self.draw_run(self.W - self.M - 14 - w, self.y, ln, fnt, size, rgb)
            self.y -= lh
        self.y -= 2

    def code(self, s):
        lines = s.split('\n'); lh = 1.45*8.2
        h = lh*len(lines) + 14
        self._ensure(h)
        self.rect(self.M, self.y - lh*len(lines) + 2, self.W-2*self.M, h, (0.96,0.96,0.97), stroke=(0.82,0.82,0.86))
        self.y -= 9
        for ln in lines:
            self.draw_run(self.M+6, self.y, ln, 'reg', 8.2, (0.16,0.2,0.26))
            self.y -= lh
        self.y -= 10

    def table(self, headers, rows, widths, rtl_cols=None):
        rtl_cols = rtl_cols or []
        size = 8.3; lh = 1.5*size; tw = (self.W-2*self.M)
        drawn = []
        for r in rows:
            wrapped = []; hmax = 1
            for i, c in enumerate(r):
                l = self.wrap_rtl(str(c), 'reg', size, widths[i]*tw - 10)
                wrapped.append(l); hmax = max(hmax, len(l))
            drawn.append((wrapped, hmax))
        self._ensure(lh+12)
        self.rect(self.M, self.y - lh + 1, tw, lh+5, (0.10,0.13,0.18))
        xx = self.W - self.M
        for i, htxt in enumerate(headers):
            cw = widths[i]*tw; w = self.text_width(htxt, 'bold', size)
            self.draw_run(xx-5-w if i in rtl_cols else xx-cw+5, self.y, htxt, 'bold', size, (1,1,1)); xx -= cw
        self.y -= lh + 5
        alt = False
        for wrapped, hmax in drawn:
            rowh = lh*hmax
            self._ensure(rowh+8)
            if alt: self.rect(self.M, self.y - rowh + 5, tw, rowh+2, (0.965,0.965,0.975))
            alt = not alt
            xx = self.W - self.M
            for i, lines in enumerate(wrapped):
                cw = widths[i]*tw; yy = self.y
                for ln in lines:
                    w = self.text_width(ln, 'reg', size)
                    self.draw_run(xx-5-w if i in rtl_cols else xx-cw+5, yy, ln, 'reg', size, (0.15,0.17,0.2)); yy -= lh
                xx -= cw
            self.y -= rowh + 2
        self.y -= 8

    def image(self, path, w, align='center'):
        key = path
        if key not in self._imgs:
            data = open(path, 'rb').read(); i = 2; W = H = 0
            while i < len(data):
                if data[i] != 0xFF: i += 1; continue
                m = data[i+1]
                if m in (0xC0,0xC1,0xC2,0xC3):
                    H, W = struct.unpack('>HH', data[i+5:i+9]); break
                ln = struct.unpack('>H', data[i+2:i+4])[0]; i += 2 + ln
            self._imgs[key] = {'data': data, 'W': W, 'H': H, 'names': []}
        rec = self._imgs[key]
        self._imgcount = getattr(self, '_imgcount', 0) + 1
        name = "Im%d" % self._imgcount
        rec['names'].append(name)
        h = w * rec['H'] / float(rec['W'])
        self._ensure(h + 12)
        x = (self.W - w)/2 if align == 'center' else self.M
        self.cur.append("q %.2f 0 0 %.2f %.2f %.2f cm /%s Do Q" % (w, h, x, self.y - h, name))
        self.y -= h + 8

    def save(self, path):
        objs = []
        def add(s): objs.append(s); return len(objs)
        fnames = {}
        for key in ('reg','bold','med'):
            f = self.fonts[key]
            ff = add("<< /Length %d /Length1 %d >> stream\n%s\nendstream" % (len(f.data), len(f.data), f.data.decode('latin1')))
            fd = add("<< /Type /FontDescriptor /FontName /VZ%s /Flags 32 /FontBBox [-500 -300 1300 1100] /ItalicAngle 0 /Ascent 1000 /Descent -300 /CapHeight 700 /StemV 80 /FontFile2 %d 0 R >>" % (key[0].upper(), ff))
            chunks = []; i = 0
            E = [(g, round(f.adv[g]*1000.0/f.upem)) for g in range(f.nglyphs)]
            while i < len(E):
                j = i
                while j+1 < len(E) and E[j+1][0] == E[j][0]+1: j += 1
                chunks.append("%d %d %s" % (E[i][0], E[j][0], " ".join(str(e[1]) for e in E[i:j+1])))
                i = j+1
            cid = add("<< /Type /Font /Subtype /CIDFontType2 /BaseName /VZ%s /CIDSystemInfo << /Registry (Adobe) /Ordering (Identity) /Supplement 0 >> /FontDescriptor %d 0 R /DW 500 /W [%s] /CIDToGIDMap /Identity >>" % (key[0].upper(), fd, " ".join(chunks)))
            fnames[key] = add("<< /Type /Font /Subtype /Type0 /BaseName /VZ%s /Encoding /Identity-H /DescendantFonts [%d 0 R] >>" % (key[0].upper(), cid))
        imgobjs = {}
        for key, rec in self._imgs.items():
            xo = add("<< /Type /XObject /Subtype /Image /Width %d /Height %d /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length %d >> stream\n%s\nendstream" % (rec['W'], rec['H'], len(rec['data']), rec['data'].decode('latin1')))
            for nm in rec['names']: imgobjs[nm] = xo
        page_ids = []
        for pg in self.pages:
            c = zlib.compress("\n".join(pg).encode('latin1'))
            cobj = add("<< /Length %d /Filter /FlateDecode >> stream\n%s\nendstream" % (len(c), c.decode('latin1')))
            res = "<< /Font << /Fr %d 0 R /Fb %d 0 R /Fm %d 0 R >>" % (fnames['reg'], fnames['bold'], fnames['med'])
            if imgobjs: res += " /XObject << " + " ".join("/%s %d 0 R" % (n, o) for n, o in imgobjs.items()) + " >>"
            res += " >>"
            page_ids.append(add("<< /Type /Page /Parent 0 0 R /MediaBox [0 0 %.2f %.2f] /Contents %d 0 R /Resources %s >>" % (self.W, self.H, cobj, res)))
        pages_id = add("<< /Type /Pages /Kids [%s] /Count %d >>" % (" ".join("%d 0 R" % p for p in page_ids), len(page_ids)))
        cat = add("<< /Type /Catalog /Pages %d 0 R >>" % pages_id)
        for pid in page_ids:
            objs[pid-1] = objs[pid-1].replace("/Parent 0 0 R", "/Parent %d 0 R" % pages_id)
        out = b"%PDF-1.6\n"
        xref = []
        for s in objs:
            xref.append(len(out))
            out += ("%d 0 obj\n" % (len(xref))).encode() + s.encode('latin1') + b"\nendobj\n"
        startx = len(out); n = len(objs)+1
        out += ("xref\n0 %d\n0000000000 65535 f \n" % n).encode()
        out += "".join("%010d 00000 n \n" % x for x in xref).encode()
        out += b"trailer\n<< /Size %d /Root %d 0 R >>\nstartxref\n%d\n%%%%EOF\n" % (n, cat, startx)
        open(path, 'wb').write(out)
        return len(self.pages)
