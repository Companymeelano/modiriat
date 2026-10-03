package ir.atiran.finance;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class FinanceAuthorizationRepositoryTest {
    @Test
    public void exposesMenuAndFormAndSubsystemGrantsWithoutCombiningTheirMeaning() {
        FinanceAuthorizationRepository.MenuEntry menu = new FinanceAuthorizationRepository.MenuEntry(
                3, "گزارشات", 1, "مالی", 10, "نمونه منو", "", 21,
                "نمونه فرم", "VerifiedFormClass", 1, "");

        Map<Integer, Set<String>> formGrants = new HashMap<>();
        formGrants.put(21, new HashSet<>(Collections.singletonList("Read")));
        Map<Integer, Set<String>> subsystemGrants = new HashMap<>();
        subsystemGrants.put(3, new HashSet<>(Collections.singletonList("Execute")));

        FinanceAuthorizationRepository.AccessSnapshot snapshot = new FinanceAuthorizationRepository.AccessSnapshot(
                42, 4, new HashSet<>(Collections.singletonList(10)), Collections.singletonList(menu),
                formGrants, subsystemGrants,
                new HashSet<>(Arrays.asList("Execute", "Read", "Write", "Edit", "Delete")));

        assertTrue(snapshot.hasMenu(10));
        assertTrue(snapshot.hasFormPermission(10, "Read"));
        assertTrue(snapshot.hasSubsystemPermission(10, "Execute"));
        assertFalse(snapshot.hasFormPermission(10, "Execute"));
        assertFalse(snapshot.hasSubsystemPermission(10, "Read"));
        assertFalse(snapshot.hasFormPermission(10, "Unknown"));
        assertFalse(snapshot.hasMenu(11));
    }

    @Test
    public void absentServerMenuMappingFailsClosedEvenWhenPermissionRowsExist() {
        Map<Integer, Set<String>> formGrants = new HashMap<>();
        formGrants.put(21, new HashSet<>(Collections.singletonList("Read")));
        Map<Integer, Set<String>> subsystemGrants = new HashMap<>();
        subsystemGrants.put(3, new HashSet<>(Collections.singletonList("Read")));

        FinanceAuthorizationRepository.AccessSnapshot snapshot = new FinanceAuthorizationRepository.AccessSnapshot(
                42, 4, new HashSet<>(Collections.singletonList(10)), Collections.<FinanceAuthorizationRepository.MenuEntry>emptyList(),
                formGrants, subsystemGrants, new HashSet<>(Collections.singletonList("Read")));

        assertFalse(snapshot.hasMenu(10));
        assertFalse(snapshot.hasFormPermission(10, "Read"));
        assertFalse(snapshot.hasSubsystemPermission(10, "Read"));
        List<FinanceAuthorizationRepository.MenuEntry> menus = snapshot.permittedMenus();
        assertTrue(menus.isEmpty());
    }
}
