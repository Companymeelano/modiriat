# FinancialSchemaMapping — آتیران مالی

این سند از **Metadata واقعی** پایگاه‌داده آتیران تولید می‌شود (فقط SELECT، فقط ساختار).
هیچ نام مشتری، مبلغ، تلفن یا متن آزاد در آن ثبت نمی‌شود؛ متن‌های بلند ماسک می‌شوند.

- زمان تولید (UTC): `2026-10-03T21:59:19.526999Z`
- پایگاه‌داده: `Atiran2` — نسخه سرور `12.0.2000.8`
- Collation: `<text 31>` — سطح سازگاری `120`
- زمان سرور: `2026-10-04 01:29:22` (UTC `2026-10-03 21:59:22`, منطقه `2026-10-03T21:59:22.5951893Z`)
- شمارش اشیا: 493 جدول، 148 ویو، 293 رویه

## ۱) جست‌وجوی نام‌های آغازین (Seed Names)

| نام درخواستی | وجود | نوع | تخمین ردیف |
|---|---|---|---|
| `CUSTOMERS` | بله | table | 2724 |
| `inventory` | بله | table | 1803 |
| `Variety` | بله | table | 1803 |
| `forosh_price` | بله | table | 1803 |
| `kagroup` | بله | table | 3 |
| `sailfact` | بله | table | 1168 |
| `subsailfact` | بله | table | 4991 |
| `buyfact` | بله | table | 62 |
| `subbuyfact` | بله | table | 211 |
| `getchk` | بله | table | 118 |
| `putchk` | بله | table | 202 |
| `CheckTypes` | بله | table | 2 |
| `BANK` | بله | table | 8 |
| `visitors` | بله | table | 10 |
| `vis_goals` | بله | table | 4 |
| `Visit` | بله | table | 0 |
| `masir` | بله | table | 4 |
| `cust_act` | بله | table | 6176 |
| `Sys_Mandeh_Customer` | بله | table | 2724 |
| `vw_customer` | بله | view | 0 |
| `TellBook` | بله | table | 2732 |

## ۲) جدول‌های نامزد مالی (مرتب‌شده بر اساس ارتباط)

| جدول | تخمین ردیف | امتیاز | ستون‌های کلیدی مالی |
|---|---|---|---|
| `sub_buyfact_temp` | 0 | 23 | `act`, `buy`, `sub_buyfact_temp.CustomerPrice`, `sub_buyfact_temp.MaxPrice`, `sub_buyfact_temp.MinPrice`, `sub_buyfact_temp.PerPos` |
| `Payslip` | 0 | 22 | `Payslip.Active`, `Payslip.AtiranDocNumber`, `Payslip.FractionWorkPrice`, `Payslip.FractionWorkTime`, `Payslip.MissionDailyPrice`, `Payslip.MissionPerMinutePrice` |
| `getchk` | 118 | 21 | `chk`, `getchk.CheckTypeID`, `getchk.DocID`, `getchk.FromAtiranDocID`, `getchk.GhnoPardakht`, `getchk.IdDocumentZirSarfasl` |
| `forosh_price` | 1803 | 19 | `forosh`, `forosh_price.DiscountForosh1`, `forosh_price.DiscountForosh2`, `forosh_price.DiscountForosh3`, `forosh_price.DiscountForosh4`, `forosh_price.DiscountForosh5` |
| `meelano_prefactors` | 13 | 17 | `act`, `factor`, `meelano_prefactors.customer_code`, `meelano_prefactors.customer_name`, `meelano_prefactors.native_prefactor_no`, `meelano_prefactors.native_prefactor_table` |
| `sailfact` | 1168 | 17 | `act`, `sail`, `sailfact.ChapWithTasvieh`, `sailfact.DocumentSourceID`, `sailfact.MabDaryaftFactor`, `sailfact.ShSanadFerestande` |
| `CUSTOMERS` | 2724 | 16 | `CUSTOMERS.BANKNAME`, `CUSTOMERS.CheckDateDay`, `CUSTOMERS.CustomerBranch`, `CUSTOMERS.CustomerTypeTtmsId`, `CUSTOMERS.MaxManFactor`, `CUSTOMERS.PriceCheck` |
| `buyfact` | 62 | 15 | `act`, `buy`, `buyfact.CashType`, `buyfact.DateModatPardakht`, `buyfact.MablaghPardakht`, `buyfact.MoeinIdKerayehHaml` |
| `cust_act` | 6176 | 15 | `act`, `cust`, `cust_act.AccDocNumber`, `cust_act.DocNumber`, `cust_act.GhestPriceTemp`, `cust_act.UserID` |
| `inventory` | 1803 | 15 | `inventory.ActiveCapillarySales`, `inventory.ActiveOnlineSales`, `inventory.FinalSalePrice`, `inventory.ImPureBuyPrice`, `inventory.MaxJozForosh`, `inventory.PerPos` |
| `Personnel` | 0 | 15 | `Personnel.Active`, `Personnel.BadWeatherAllowancePrice`, `Personnel.BankAccountNumber`, `Personnel.BankName`, `Personnel.CardNumber`, `Personnel.ChildAllowancePrice` |
| `putchk` | 202 | 15 | `chk`, `putchk.CheckTypeID`, `putchk.DocID`, `putchk.FromAtiranDocID`, `putchk.IdDocumentZirSarfasl`, `putchk.UserID` |
| `ban_act` | 1232 | 14 | `act`, `ban_act.AccDocNumber`, `ban_act.DocNumber`, `ban_act.UserID`, `ban_act.act_bed`, `ban_act.act_bes` |
| `ka_act` | 6359 | 14 | `act`, `ka_act.HAct_id`, `ka_act.PriceFinished`, `ka_act.RdfAnbar`, `ka_act.ShSanadAnbar`, `ka_act.UserID` |
| `PardakhtKerayeHamlDetails` | 0 | 14 | `PardakhtKerayeHamlDetails.ActID`, `PardakhtKerayeHamlDetails.Active`, `PardakhtKerayeHamlDetails.Price`, `PardakhtKerayeHamlDetails.RdfBank`, `PardakhtKerayeHamlDetails.RdfPutCheck`, `PardakhtKerayeHamlDetails.RdfZirSarfasl` |
| `sailfact_pish` | 13 | 14 | `act`, `sail`, `sailfact_pish.RejectedUser`, `sailfact_pish.USER__`, `sailfact_pish.UserTaedForush`, `sailfact_pish.UserTaedHesabdari` |
| `TerminalPos` | 0 | 14 | `TerminalPos.Active`, `TerminalPos.TerminalCompanyID`, `TerminalPos.TerminalID`, `TerminalPos.TerminalIP`, `TerminalPos.TerminalName`, `TerminalPos.TerminalNumber` |
| `DaryaftVaPardakht` | 0 | 13 | `DaryaftVaPardakht.Active`, `DaryaftVaPardakht.IsDaryaft`, `DaryaftVaPardakht.Naghd`, `DaryaftVaPardakht.SumCheck`, `DaryaftVaPardakht.SumPos`, `DaryaftVaPardakht.UserID` |
| `BANK` | 8 | 12 | `BANK.Active`, `BANK.BANKNAME`, `BANK.BankRdf`, `BANK.BankTransferCode`, `BANK.CardNumber`, `BANK.HaveEChecks` |
| `DeviceSettings` | 5 | 12 | `DeviceSettings.ActsLimit`, `DeviceSettings.CanDirectDaryaft`, `DeviceSettings.CheckCredit`, `DeviceSettings.CheckingBouncedCheck`, `DeviceSettings.DefaultPriceGrp`, `DeviceSettings.HasAccessPishDaryaft` |
| `PardakhtMultiFactor` | 0 | 12 | `PardakhtMultiFactor.GhnoPardakht`, `PardakhtMultiFactor.Mablagh`, `PardakhtMultiFactor.ShBuyFact`, `act`, `factor`, `pardakht` |
| `PayrollPayment` | 0 | 12 | `PayrollPayment.Active`, `PayrollPayment.BankID`, `PayrollPayment.BankTransfer`, `PayrollPayment.BankTransferPrice`, `PayrollPayment.CashPayment`, `PayrollPayment.CashPaymentPrice` |
| `subbuyfact_pish` | 0 | 12 | `act`, `buy`, `subbuyfact_pish.JOZPRICE`, `subbuyfact_pish.VAHPRICE`, `subbuyfact_pish.active`, `subbuyfact_pish.anbarName` |
| `subsailfact` | 4991 | 12 | `act`, `sail`, `subsailfact.JOZPRICE`, `subsailfact.TafifNaghd`, `subsailfact.TafifPos`, `subsailfact.VAHPRICE` |
| `template_for_putchk` | 0 | 12 | `chk`, `template_for_putchk.IdDocumentZirSarfasl`, `template_for_putchk.UserID`, `template_for_putchk.bankrdf`, `template_for_putchk.chkbatch_num`, `template_for_putchk.putchk_dis` |
| `BankPos` | 0 | 11 | `BankPos.Active`, `BankPos.BankID`, `BankPos.BankPosID`, `BankPos.TerminalPosID`, `BankPos.UserID`, `bank` |
| `buyfact_pish` | 0 | 11 | `act`, `buy`, `buyfact_pish.CustGroupRdf`, `buyfact_pish.FactorShode`, `buyfact_pish.UserID`, `buyfact_pish.active` |
| `ChangeStateFactorForoshTemp` | 0 | 11 | `ChangeStateFactorForoshTemp.FactorNumber`, `ChangeStateFactorForoshTemp.UserID`, `act`, `factor`, `forosh` |
| `ChangeStateFactorKharidTemp` | 0 | 11 | `ChangeStateFactorKharidTemp.FactorNumber`, `ChangeStateFactorKharidTemp.UserID`, `act`, `factor`, `kharid` |
| `DaryaftMultiFactor` | 0 | 11 | `DaryaftMultiFactor.IsTasvieh`, `DaryaftMultiFactor.Price`, `act`, `daryaft`, `factor` |
| `PishDaryaftMultiFactor` | 0 | 11 | `PishDaryaftMultiFactor.GhnoPishDaryaft`, `PishDaryaftMultiFactor.Price`, `act`, `daryaft`, `factor` |
| `subsailtemp` | 0 | 11 | `sail`, `subsailtemp.MultiPishFactor`, `subsailtemp.TafifNaghd`, `subsailtemp.TafifPos`, `subsailtemp.UserID`, `subsailtemp.active` |
| `Sys_Mandeh_Customer` | 2724 | 11 | `Sys_Mandeh_Customer.CheckAddToMandeh`, `Sys_Mandeh_Customer.CreditCheckType`, `Sys_Mandeh_Customer.Mandeh`, `Sys_Mandeh_Customer.MaxCheckPassNashode`, `Sys_Mandeh_Customer.MaxSarCheck`, `cust` |
| `sys_users` | 6 | 11 | `sys_users.SysuserTransferCode`, `sys_users.active`, `sys_users.user_fname`, `sys_users.user_id`, `sys_users.user_lname`, `sys_users.user_name` |
| `UserPos` | 0 | 11 | `UserPos.Active`, `UserPos.BankPoseID`, `UserPos.ConfirmedUserID`, `UserPos.UserID`, `UserPos.UserPosID`, `pos` |
| `back_sanad` | 77 | 10 | `back_sanad.Active`, `back_sanad.DeleteUser`, `back_sanad.TaeedUserId`, `back_sanad.TaeedWarehousUserId`, `back_sanad.UserID`, `back_sanad.VisitorId` |
| `meelano_prefactor_items` | 17 | 10 | `act`, `factor`, `meelano_prefactor_items.amount`, `meelano_prefactor_items.prefactor_id`, `meelano_prefactor_items.price`, `meelano_prefactor_items.price_tier` |
| `PersonnelGroupInformation` | 0 | 10 | `PersonnelGroupInformation.BadWeatherAllowancePrice`, `PersonnelGroupInformation.ChildAllowancePrice`, `PersonnelGroupInformation.DailyMissionPrice`, `PersonnelGroupInformation.HousingAllowancePrice`, `PersonnelGroupInformation.MealStipendPrice`, `PersonnelGroupInformation.MissionPrice` |
| `PishDaryaftGetCheck` | 0 | 10 | `PishDaryaftGetCheck.BankName`, `PishDaryaftGetCheck.GhnoPishDaryaft`, `PishDaryaftGetCheck.Price`, `PishDaryaftGetCheck.ShomareHesab`, `check`, `daryaft` |
| `PishDaryaftPos` | 0 | 10 | `PishDaryaftPos.GhnoPishDaryaft`, `PishDaryaftPos.PosBankRdf`, `PishDaryaftPos.ShomarePeygiri`, `PishDaryaftPos.price`, `daryaft`, `pos` |
| `PosDetails` | 1083 | 10 | `PosDetails.MabPos`, `PosDetails.PosBankRdf`, `PosDetails.PosDesc`, `PosDetails.RdfZirSarFasl`, `PosDetails.TerminalID`, `PosDetails.UserID` |
| `subAnbarSanad` | 0 | 10 | `anbar`, `sanad`, `subAnbarSanad.Active`, `subAnbarSanad.UserId`, `subAnbarSanad.anbarID`, `subAnbarSanad.anbarSanadID` |
| `subsailfact_pish` | 15 | 10 | `act`, `sail`, `subsailfact_pish.JOZPRICE`, `subsailfact_pish.VAHPRICE`, `subsailfact_pish.active`, `subsailfact_pish.rdf_anbar` |
| `visitors` | 10 | 10 | `visitor`, `visitors.CountCustomerBed`, `visitors.TedadFactorMojazMande`, `visitors.UserID`, `visitors.Username`, `visitors.active` |
| `WorkingConvention` | 0 | 10 | `WorkingConvention.BadWeatherAllowancePrice`, `WorkingConvention.ChildAllowancePrice`, `WorkingConvention.DailyMissionPrice`, `WorkingConvention.HousingAllowancePrice`, `WorkingConvention.MealStipendPrice`, `WorkingConvention.MissionPrice` |
| `act_zirsarfasls` | 291 | 9 | `act`, `act_zirsarfasls.UserID`, `act_zirsarfasls.rdf_zirsarfasls`, `act_zirsarfasls.sanadno`, `sarfasl` |
| `AnbarSanad` | 0 | 9 | `AnbarSanad.Active`, `AnbarSanad.sh_sanad`, `AnbarSanad.userID`, `anbar`, `sanad` |
| `chks_temp` | 0 | 9 | `chk`, `chks_temp.GhnoPardakht`, `chks_temp.UserID`, `chks_temp.act`, `chks_temp.getchkmab`, `chks_temp.getchkshhes` |
| `ConfirmUser` | 968 | 9 | `ConfirmUser.FakeUserID`, `ConfirmUser.FakeUserName`, `ConfirmUser.RealUserID`, `ConfirmUser.RealUserName`, `ConfirmUser.SumCheck`, `ConfirmUser.SumPos` |
| `PishDaryaft` | 0 | 9 | `PishDaryaft.ExtraPrice`, `PishDaryaft.GhnoPishDaryaft`, `PishDaryaft.Naghd`, `PishDaryaft.UserRejectedHesabdari`, `PishDaryaft.UserTaeedHesabdari`, `PishDaryaft.VisitorID` |
| `subbuyfact` | 211 | 9 | `act`, `buy`, `subbuyfact.JOZPRICE`, `subbuyfact.active`, `subbuyfact.rdf_anbar` |
| `UserAnbar` | 0 | 9 | `UserAnbar.Active`, `UserAnbar.AnbarID`, `UserAnbar.UserID`, `anbar`, `user` |
| `ZamanBandiTasviehFactor` | 0 | 9 | `act`, `factor`, `tasvieh` |
| `ZirSarFaslDocuments` | 231 | 9 | `ZirSarFaslDocuments.Active`, `ZirSarFaslDocuments.SanadNumber`, `ZirSarFaslDocuments.UserID`, `doc`, `sarfasl` |
| `AnbarDifferent` | 0 | 8 | `AnbarDifferent.Active`, `AnbarDifferent.AnbarID`, `AnbarDifferent.ComparisonDocNumber`, `AnbarDifferent.JozPrice`, `AnbarDifferent.SanadNo`, `anbar` |
| `backsail_temp` | 0 | 8 | `backsail_temp.PriceFinished`, `backsail_temp.UserID`, `backsail_temp.jozprice`, `backsail_temp.sh_back_sanad`, `backsail_temp.vahprice`, `sail` |
| `chkbatch` | 15 | 8 | `chk`, `chkbatch.Active`, `chkbatch.BANKRDF`, `chkbatch.UserID`, `chkbatch.chkbatch_num`, `chkbatch.rdf_bank` |
| `customerComputers` | 0 | 8 | `cust`, `customerComputers.ActiveStatus`, `customerComputers.CustomerSHMO`, `customerComputers.NameActiveStatus`, `customerComputers.customerComputerID`, `customerComputers.userFullName` |
| `dar` | 1063 | 8 | `dar.Active`, `dar.DocumentSourceID`, `dar.OtherPriceForFactor`, `dar.TaeedUserID`, `dar.UserID`, `dar.mabcheck` |
| `Document` | 0 | 8 | `Document.Active`, `Document.DocDesc`, `Document.DocID`, `Document.DocNumber`, `Document.MainDocNumber`, `doc` |

## ۳) ستون‌های جدول‌های مالی کلیدی

### `<text 30>` — 0 ردیف (تخمین)
- کلیدها: <text 33>(PK)=RowID
- FK `<text 43>`: CustGroupID → custgroup.group_rdf
- FK `<text 42>`: OsystemID → osystems.rdf_system

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `CustGroupID` | int(4) | خیر | — |  |
| `OsystemID` | int(4) | خیر | — |  |
| `PercentageDiscount` | decimal(9) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `<text 31>` — 0 ردیف (تخمین)
- کلیدها: <text 34>(PK)=ID
- FK `<text 35>`: PersonnelID → Personnel.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `AccBaseSalary` | int(4) | بله | — |  |
| `AccEndOfWork` | int(4) | بله | — |  |
| `AccOverTimes` | int(4) | بله | — |  |
| `AccNightWork` | int(4) | بله | — |  |
| `AccMission` | int(4) | بله | — |  |
| `AccShiftWork` | int(4) | بله | — |  |
| `AccWorkInVacation` | int(4) | بله | — |  |
| `AccEndOfWorkBenefit` | int(4) | بله | — |  |
| `AccHousingAllowance` | int(4) | بله | — |  |
| `AccNewYearGift` | int(4) | بله | — |  |
| `AccTransportation` | int(4) | بله | — |  |
| `AccHireAllowance` | int(4) | بله | — |  |
| `AccMealStipend` | int(4) | بله | — |  |
| `AccCreditOfLeave` | int(4) | بله | — |  |
| `AccSupervisionAllowance` | int(4) | بله | — |  |
| `AccRewardPrice` | int(4) | بله | — |  |
| `AccBadWeatherAllowance` | int(4) | بله | — |  |
| `AccChildAllowance` | int(4) | بله | — |  |
| `AccOtherBenefits` | int(4) | بله | — |  |
| `AccEmployerInsurance` | int(4) | بله | — |  |
| `AccUnemploymentInsurance` | int(4) | بله | — |  |
| `AccRoundAddition` | int(4) | بله | — |  |
| `AccPersonnelCredit` | int(4) | بله | — |  |
| `AccTaxOnSalary` | int(4) | بله | — |  |
| `AccInsurance` | int(4) | بله | — |  |
| `AccAdditionInsurance` | int(4) | بله | — |  |
| `AccAdvanceMoney` | int(4) | بله | — |  |
| `AccLoanInstallment` | int(4) | بله | — |  |
| `AccPayrollDeduction` | int(4) | بله | — |  |
| `AccOtherDeduction` | int(4) | بله | — |  |
| `AccRoundDeduction` | int(4) | بله | — |  |

### `<text 33>` — 0 ردیف (تخمین)
- کلیدها: <text 36>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `text` | nvarchar(1000) | خیر | — |  |

### `<text 34>` — 0 ردیف (تخمین)
- کلیدها: <text 37>(PK)=ID
- FK `<text 48>`: ConventionID → convention.conventionID
- FK `<text 55>`: ClientID → customerComputers.customerComputerID
- FK `<text 53>`: TabletID → CustomerTablets.cusotmerTabletID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `TabletID` | int(4) | بله | — |  |
| `ClientID` | int(4) | بله | — |  |
| `ConventionID` | int(4) | بله | — |  |

### `<text 35>` — 0 ردیف (تخمین)
- کلیدها: <text 38>(PK)=ID
- FK `<text 53>`: PersonnelGroupID → PersonnelGroup.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PersonnelGroupID` | int(4) | خیر | — |  |
| `AccBaseSalary` | int(4) | بله | — |  |
| `AccEndOfWork` | int(4) | بله | — |  |
| `AccOverTimes` | int(4) | بله | — |  |
| `AccNightWork` | int(4) | بله | — |  |
| `AccMission` | int(4) | بله | — |  |
| `AccShiftWork` | int(4) | بله | — |  |
| `AccWorkInVacation` | int(4) | بله | — |  |
| `AccEndOfWorkBenefit` | int(4) | بله | — |  |
| `AccHousingAllowance` | int(4) | بله | — |  |
| `AccNewYearGift` | int(4) | بله | — |  |
| `AccTransportation` | int(4) | بله | — |  |
| `AccHireAllowance` | int(4) | بله | — |  |
| `AccMealStipend` | int(4) | بله | — |  |
| `AccCreditOfLeave` | int(4) | بله | — |  |
| `AccSupervisionAllowance` | int(4) | بله | — |  |
| `AccRewardPrice` | int(4) | بله | — |  |
| `AccBadWeatherAllowance` | int(4) | بله | — |  |
| `AccChildAllowance` | int(4) | بله | — |  |
| `AccOtherBenefits` | int(4) | بله | — |  |
| `AccEmployerInsurance` | int(4) | بله | — |  |
| `AccUnemploymentInsurance` | int(4) | بله | — |  |
| `AccRoundAddition` | int(4) | بله | — |  |
| `AccPersonnelCredit` | int(4) | بله | — |  |
| `AccTaxOnSalary` | int(4) | بله | — |  |
| `AccInsurance` | int(4) | بله | — |  |
| `AccAdditionInsurance` | int(4) | بله | — |  |
| `AccAdvanceMoney` | int(4) | بله | — |  |
| `AccLoanInstallment` | int(4) | بله | — |  |
| `AccPayrollDeduction` | int(4) | بله | — |  |
| `AccOtherDeduction` | int(4) | بله | — |  |
| `AccRoundDeduction` | int(4) | بله | — |  |

### `<text 39>` — 0 ردیف (تخمین)
- کلیدها: <text 42>(PK)=rowId
- FK `<text 48>`: MoeinIdBed → Moein.MoeinID
- FK `<text 49>`: MoeinIdBes → Moein.MoeinID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rowId` | bigint(8) | خیر | بله |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `MoeinIdBed` | bigint(8) | بله | — |  |
| `MoeinIdBes` | bigint(8) | بله | — |  |
| `TransferCode` | nvarchar(100) | بله | — |  |

### `AccountTypes` — 2 ردیف (تخمین)
- کلیدها: PK_AccountTypes(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | tinyint(1) | خیر | بله |  |
| `AccountTypeName` | nvarchar(100) | خیر | — |  |

### `AccountingMapping` — 37 ردیف (تخمین)
- کلیدها: PK_AccountingMapping(PK)=MappingID
- FK `FK_AccountingMapping_Kol`: KolID → Kol.KolID
- FK `FK_AccountingMapping_Moein`: MoeinID → Moein.MoeinID
- FK `FK_AccountingMapping_Tafsil`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MappingID` | int(4) | خیر | — |  |
| `AtiranName` | nvarchar(2000) | خیر | — |  |
| `KolID` | bigint(8) | بله | — |  |
| `MoeinID` | bigint(8) | بله | — |  |
| `TafsilID` | bigint(8) | بله | — |  |
| `IsEdit` | bit(1) | خیر | — | ((0)) |
| `Level` | tinyint(1) | بله | — |  |

### `Accounts` — 33 ردیف (تخمین)
- کلیدها: PK_Accounts(PK)=Id
- FK `FK_Accounts_AccountTypes`: AccountTypeId → AccountTypes.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `AccountName` | nvarchar(200) | خیر | — |  |
| `AccountTypeId` | tinyint(1) | خیر | — |  |

### `ActIDInfo` — 75 ردیف (تخمین)
- کلیدها: PK_ActIDInfo(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `ActID` | int(4) | خیر | — |  |
| `HactID` | int(4) | بله | — |  |
| `Explain` | nvarchar(1000) | خیر | — |  |
| `TableName` | nvarchar(1000) | خیر | — |  |

### `ActNames` — 120 ردیف (تخمین)
- کلیدها: PK_ActNames(PK)=ActID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ActID` | int(4) | خیر | — |  |
| `ActName` | nvarchar(1000) | خیر | — |  |

### `ActSubmittedTax` — 0 ردیف (تخمین)

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Description` | nvarchar(200) | خیر | — |  |
| `SubmittedTaxID` | int(4) | خیر | — |  |

### `AdditionInsurance` — 0 ردیف (تخمین)
- کلیدها: PK_AdditionInsurance(PK)=ID
- FK `<text 30>`: PersonnelID → Personnel.ID
- FK `<text 30>`: InsertBy → sys_users.user_id
- FK `<text 31>`: UpdateBy → sys_users.user_id
- FK `<text 30>`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `AdditionInsurancePrice` | money(8) | خیر | — |  |
| `InstallmentCount` | smallint(2) | خیر | — |  |
| `StartDateInstallment` | date(3) | خیر | — |  |
| `InstallmentIntervals` | smallint(2) | خیر | — |  |
| `InstallmentPrice` | money(8) | خیر | — |  |
| `FirstInstallmentYear` | smallint(2) | بله | — |  |
| `FirstInstallmentMonth` | smallint(2) | بله | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `AdditionInsuranceInstallment` — 0 ردیف (تخمین)
- کلیدها: <text 31>(PK)=ID
- FK `<text 49>`: AdditionInsuranceID → AdditionInsurance.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `AdditionInsuranceID` | int(4) | خیر | — |  |
| `PaymentDate` | date(3) | خیر | — |  |
| `Price` | money(8) | خیر | — |  |
| `Month` | smallint(2) | بله | — |  |
| `Year` | smallint(2) | بله | — |  |
| `IsPaid` | bit(1) | بله | — |  |

### `AdvanceMoney` — 0 ردیف (تخمین)
- کلیدها: PK_AdvanceMoney(PK)=ID
- FK `<text 31>`: BankInformationID → BankInformation.ID
- FK `FK_AdvanceMoney_Personnel`: PersonnelID → Personnel.ID
- FK `<text 30>`: SettlementTypeID → SettlementType.ID
- FK `FK_AdvanceMoney_sys_users`: InsertBy → sys_users.user_id
- FK `FK_AdvanceMoney_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_AdvanceMoney_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `AdvanceMoneyPrice` | money(8) | خیر | — |  |
| `PaymentShamsiDate` | nchar(20) | خیر | — |  |
| `PaymentDate` | datetime(8) | خیر | — |  |
| `SettlementTypeID` | int(4) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `BankInformationID` | int(4) | بله | — |  |
| `RequestDate` | datetime(8) | بله | — |  |
| `RequestShamsiDate` | nchar(20) | بله | — |  |

### `Aghsat` — 0 ردیف (تخمین)
- کلیدها: PK_Aghsat(PK)=GhestID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GhestID` | int(4) | خیر | بله |  |
| `Shmo` | int(4) | خیر | — |  |
| `FirstGhestDate` | nvarchar(20) | خیر | — |  |
| `GhestCount` | int(4) | خیر | — |  |
| `GhestIntervalMonth` | int(4) | خیر | — |  |
| `GhestIntervalDay` | int(4) | خیر | — |  |
| `GhestPrice` | decimal(9) | خیر | — |  |
| `GhestJarime` | decimal(9) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `DateClient` | nvarchar(20) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(100) | خیر | — |  |
| `RialJarime` | decimal(9) | خیر | — |  |
| `GhestPriceWithJarime` | decimal(9) | خیر | — |  |
| `Explain` | nvarchar(1000) | خیر | — |  |
| `AllPrice` | decimal(9) | خیر | — | ((0)) |

### `AghsatSub` — 0 ردیف (تخمین)
- کلیدها: PK_AghsatSub(PK)=RowID
- FK `FK_AghsatSub_Aghsat`: GhestID → Aghsat.GhestID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `GhestID` | int(4) | بله | — |  |
| `DateGhest` | nvarchar(20) | بله | — |  |
| `Price` | decimal(9) | بله | — | ((0)) |
| `PriceOfFine` | decimal(9) | بله | — | ((0)) |
| `GhnoDaryaft` | int(4) | بله | — | ((0)) |

### `AnbarDifferent` — 0 ردیف (تخمین)
- کلیدها: PK_AnbarDifferent(PK)=RowID, Shka, SanadNo, Kind, rdf_
- FK `FK_AnbarDifferent_anbars`: AnbarID → anbars.rdf_anbar
- FK `FK_AnbarDifferent_inventory`: Shka → inventory.shka
- FK `<text 30>`: SanadNo → kasr_e_sanad.rdf
- FK `<text 30>`: Kind → kasr_e_sanad.kasr_e
- FK `FK_AnbarDifferent_Variety`: VarietyID → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `SanadNo` | int(4) | خیر | — |  |
| `Kind` | int(4) | خیر | — |  |
| `AnbarID` | int(4) | خیر | — |  |
| `JozPrice` | decimal(9) | خیر | — |  |
| `TedVahOld` | decimal(9) | خیر | — |  |
| `TedJozOld` | decimal(9) | خیر | — |  |
| `TedVahCounted` | decimal(9) | خیر | — |  |
| `TedJozCounted` | decimal(9) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `ComparisonDocNumber` | int(4) | بله | — |  |
| `FinalTedVah` | decimal(9) | خیر | — | ((0)) |
| `FinalTedJoz` | decimal(9) | خیر | — | ((0)) |
| `rdf_` | bigint(8) | خیر | بله |  |
| `VarietyID` | int(4) | خیر | — |  |

### `AnbarSanad` — 0 ردیف (تخمین)
- کلیدها: PK_AnbarSenad(PK)=Id
- FK `FK_AnbarSanad_anbarSanadType`: type → anbarSanadType.id
- FK `FK_AnbarSanad_osystems`: sysID → osystems.rdf_system
- FK `FK_AnbarSanad_sys_users`: userID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | bigint(8) | خیر | بله |  |
| `userID` | int(4) | خیر | — |  |
| `shmo` | int(4) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `time` | char(10) | خیر | — |  |
| `sh_sanad` | bigint(8) | خیر | — |  |
| `type` | int(4) | خیر | — |  |
| `sysID` | int(4) | خیر | — |  |
| `freightNumber` | nvarchar(100) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `ShFac` | bigint(8) | خیر | — | ((0)) |
| `Active` | bit(1) | خیر | — | ((1)) |
| `IsComplet` | bit(1) | خیر | — | ((0)) |

### `AppAccess` — 93 ردیف (تخمین)
- کلیدها: PK_AppAccess(PK)=ID
- FK `FK_AppAccess_AppAccessTypes`: AppAccessTypeID → AppAccessTypes.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Name` | nvarchar(200) | خیر | — |  |
| `Description` | nvarchar(200) | خیر | — |  |
| `ParentID` | int(4) | بله | — |  |
| `FormName` | nvarchar(200) | بله | — |  |
| `Level` | smallint(2) | بله | — |  |
| `AppAccessTypeID` | int(4) | بله | — |  |

### `AppAccessTypes` — 6 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `TypeName` | nvarchar(100) | خیر | — |  |
| `Description` | nvarchar(400) | بله | — |  |

### `AssignSaleInvoiceToWorker` — 0 ردیف (تخمین)
- کلیدها: PK_AssignSaleToWorker_1(PK)=Id
- FK `<text 38>`: UserId → sys_users.user_id
- FK `FK_AssignSaleToWorker_worker`: WorkerId → worker.worker_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `WorkerId` | int(4) | خیر | — |  |
| `SaleId` | bigint(8) | خیر | — |  |
| `Date` | nvarchar(20) | خیر | — |  |
| `Time` | nvarchar(12) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |

### `AtiranKindDocument` — 65 ردیف (تخمین)
- کلیدها: PK_AtiranKindDocument(PK)=KindCode

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `KindID` | int(4) | خیر | بله |  |
| `KindCode` | int(4) | خیر | — |  |
| `KindName` | nvarchar(100) | خیر | — |  |

### `AtiranSettings` — 0 ردیف (تخمین)
- کلیدها: PK_AtiranSettings(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ServerName` | nvarchar(100) | بله | — |  |
| `DatabaseName` | nvarchar(100) | بله | — |  |
| `WindowsAuthentication` | bit(1) | بله | — |  |
| `UserName` | nvarchar(200) | بله | — |  |
| `Password` | varbinary(1000) | بله | — |  |
| `MergeItems` | bit(1) | بله | — |  |
| `SplitPersonnelClaim` | bit(1) | خیر | — | ((0)) |

### `AttachmentKind` — 0 ردیف (تخمین)
- کلیدها: PK_AttachmentKind(PK)=AttachmentKindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `AttachmentKindID` | int(4) | خیر | بله |  |
| `AttachmentKindName` | nvarchar(100) | بله | — |  |
| `AttachmentKindType` | nvarchar(100) | بله | — |  |

### `Attendance` — 0 ردیف (تخمین)
- کلیدها: PK_Attendance(PK)=ID
- FK `FK_Attendance_Personnel`: PersonnelID → Personnel.ID
- FK `FK_Attendance_sys_users`: InsertBy → sys_users.user_id
- FK `FK_Attendance_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_Attendance_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `EntryTime` | nchar(20) | خیر | — |  |
| `ExitTime` | nchar(20) | خیر | — |  |
| `ShamsiDate` | nchar(20) | خیر | — |  |
| `Date` | datetime(8) | خیر | — |  |
| `ShiftWork` | bit(1) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `AttendanceDevicePatterns` — 5 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `<text 49>`: AttendancePatternTypeID → AttendancePatternType.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PatternName` | nvarchar(400) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |
| `AttendancePatternTypeID` | int(4) | بله | — |  |
| `ServerName` | nvarchar(100) | بله | — |  |
| `DatabaseName` | nvarchar(100) | بله | — |  |
| `WindowsAuthentication` | bit(1) | بله | — |  |
| `UserName` | nvarchar(200) | بله | — |  |
| `Password` | nvarchar(400) | بله | — |  |
| `Query` | nvarchar(1000) | بله | — |  |
| `Code` | nvarchar(100) | بله | — |  |

### `AttendancePatternType` — 2 ردیف (تخمین)
- کلیدها: PK_AttendancePatternType(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Type` | nvarchar(600) | بله | — |  |

### `BANK` — 8 ردیف (تخمین)
- کلیدها: PK_BANK(PK)=RDF
- FK `FK_BANK_BANK_NAME`: BankRdf → BANK_NAME.RDF
- FK `FK_BANK_Moein`: MoeinId → Moein.MoeinID
- FK `FK_BANK_sys_users`: UserID → sys_users.user_id
- FK `FK_BANK_Tafsil`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | بله |  |
| `START_DATE` | char(10) | خیر | — |  |
| `OWNER` | nvarchar(1000) | خیر | — | ('ذکر نشده') |
| `BANKNAME` | nvarchar(1000) | خیر | — |  |
| `SHOBE` | nvarchar(1000) | خیر | — | ('ذکر نشده') |
| `ADDRE` | nvarchar(1600) | خیر | — | ('ذکر شده') |
| `TELL1` | nvarchar(1000) | خیر | — | ('ذکر نشده') |
| `TELL2` | nvarchar(1000) | خیر | — | ('ذکر نشده') |
| `SHHE` | nvarchar(1000) | خیر | — |  |
| `MAN` | money(8) | خیر | — |  |
| `AccountType` | int(4) | بله | — |  |
| `BranchCode` | int(4) | بله | — |  |
| `TafsilID` | bigint(8) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `BankRdf` | int(4) | بله | — |  |
| `IsPos` | bit(1) | بله | — | ((0)) |
| `IsCard` | bit(1) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `BankTransferCode` | nvarchar(100) | بله | — |  |
| `CardNumber` | nvarchar(38) | بله | — |  |
| `ShabaNumber` | nvarchar(100) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — |  |
| `HaveEChecks` | bit(1) | بله | — |  |
| `BlackList` | bit(1) | خیر | — | ((0)) |

### `BANK_NAME` — 26 ردیف (تخمین)
- کلیدها: PK_BANK_NAME(PK)=RDF

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | بله |  |
| `NAMES` | varchar(40) | خیر | — |  |
| `Active` | bit(1) | بله | — |  |

### `BankInformation` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `FK_BankInformation_Banks`: BankID → Banks.ID
- FK `FK_BankInformation_Personnel`: PersonnelID → Personnel.ID
- FK `FK_BankInformation_sys_users`: InsertBy → sys_users.user_id
- FK `<text 29>`: UpdateBy → sys_users.user_id
- FK `FK_BankInformation_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `PersonnelID` | int(4) | بله | — |  |
| `ShabaNumber` | nvarchar(600) | بله | — |  |
| `BankID` | int(4) | بله | — |  |
| `BankName` | nvarchar(600) | بله | — |  |
| `BranchName` | nvarchar(600) | بله | — |  |
| `AccountNumber` | nvarchar(400) | بله | — |  |
| `CardNumber` | nvarchar(600) | بله | — |  |
| `DefaultAccount` | int(4) | بله | — |  |
| `Description` | nvarchar(600) | بله | — |  |
| `InsertBy` | int(4) | خیر | — |  |
| `InsertSystemDateTime` | datetime(8) | خیر | — |  |
| `InsertServerDateTime` | datetime(8) | خیر | — |  |
| `InsertShamsiDate` | nchar(20) | خیر | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | خیر | — |  |
| `Active` | bit(1) | بله | — |  |

### `BankPos` — 0 ردیف (تخمین)
- کلیدها: PK_BankPos(PK)=BankPosID
- FK `FK_BankPos_BANK`: BankID → BANK.RDF
- FK `FK_BankPos_sys_users`: UserID → sys_users.user_id
- FK `FK_BankPos_TerminalPos`: TerminalPosID → TerminalPos.TerminalID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `BankPosID` | int(4) | خیر | بله |  |
| `BankID` | int(4) | خیر | — |  |
| `TerminalPosID` | int(4) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(20) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |

### `Banks` — 0 ردیف (تخمین)
- کلیدها: PK_Banks(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `BankName` | nvarchar(300) | بله | — |  |

### `BrandName` — 0 ردیف (تخمین)
- کلیدها: PK_BrandName(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Brand` | nvarchar(1000) | خیر | — |  |

### `BusinessCategory` — 0 ردیف (تخمین)
- کلیدها: PK_BusinessCategory(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `Category` | nvarchar(400) | خیر | — |  |
| `Description` | nvarchar(1000) | بله | — |  |

### `CITYS` — 2 ردیف (تخمین)
- کلیدها: PK_CITYS(PK)=RDF
- FK `FK_CITYS_Province`: ProvinceID → Province.ProvinceID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | بله |  |
| `name` | varchar(40) | خیر | — |  |
| `ProvinceID` | int(4) | خیر | — |  |
| `Lat` | float(8) | بله | — |  |
| `Lng` | float(8) | بله | — |  |
| `TemoCityID` | int(4) | بله | — |  |
| `CodeTTMS` | nvarchar(100) | بله | — |  |
| `Code` | nvarchar(1000) | خیر | — |  |

### `CITYS_Temp` — 481 ردیف (تخمین)
- کلیدها: PK_CITYS_Temp(PK)=RDF

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | بله |  |
| `ProvinceID` | int(4) | بله | — |  |
| `name` | varchar(40) | خیر | — |  |
| `Lat` | float(8) | بله | — |  |
| `Lng` | float(8) | بله | — |  |

### `COW` — 77 ردیف (تخمین)
- کلیدها: PK_COW(PK)=rdf
- FK `FK_COW_osystems`: SysID → osystems.rdf_system
- FK `FK_COW_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `DATE` | char(10) | خیر | — |  |
| `DIS` | varchar(500) | خیر | — |  |
| `BED` | money(8) | خیر | — | (0) |
| `BES` | money(8) | خیر | — | (0) |
| `rdf` | bigint(8) | خیر | بله |  |
| `done_act` | char(10) | بله | — |  |
| `act_id` | int(4) | بله | — |  |
| `bank_rdf` | int(4) | بله | — | ((0)) |
| `DocNumber` | int(4) | بله | — |  |
| `SysID` | int(4) | خیر | — | ((1)) |
| `Time` | nvarchar(40) | بله | — |  |
| `isActive` | bit(1) | بله | — | ((1)) |
| `Ghno` | int(4) | بله | — |  |
| `AccDocNumber` | int(4) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `isEdited` | bit(1) | خیر | — | ((0)) |

### `CUSTOMERS` — 2724 ردیف (تخمین)
- کلیدها: PK_CUSTOMERS(PK)=SHMO
- FK `<text 29>`: BusinessCategoryId → BusinessCategory.Id
- FK `FK_CUSTOMERS_custgroup`: group_rdf → custgroup.group_rdf
- FK `<text 29>`: CustomerTypeTtmsId → CustomerTypeTTMS.ID
- FK `FK_CUSTOMERS_Ethadie`: EtehadieID → Ethadie.RDF
- FK `FK_CUSTOMERS_masir`: RDF_masir → masir.rdf_masir
- FK `FK_CUSTOMERS_Tafsil`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SHMO` | int(4) | خیر | بله |  |
| `MONAME` | nvarchar(1000) | خیر | — |  |
| `code` | nvarchar(500) | خیر | — | ('ذکر نشده') |
| `SHHES` | nvarchar(400) | خیر | — | ('ذکر نشده') |
| `BANKNAME` | nvarchar(600) | خیر | — | ('ذکر نشده') |
| `bankshobe` | nvarchar(600) | خیر | — | ('ذکر نشده') |
| `addre` | nvarchar(4000) | خیر | — | ('ذکر نشده') |
| `tell1` | nvarchar(400) | خیر | — | ('ذکر نشده') |
| `tell2` | nvarchar(400) | خیر | — | ('ذکرنشده') |
| `cell` | nvarchar(400) | خیر | — | ('ذکر نشده') |
| `active` | char(1) | خیر | — | ('T') |
| `cred` | money(8) | خیر | — | ((0)) |
| `man` | money(8) | خیر | — |  |
| `peygham1` | nvarchar(200) | خیر | — | ('ذکرنشده') |
| `special` | char(1) | خیر | — |  |
| `group_rdf` | int(4) | خیر | — | ((0)) |
| `date` | char(10) | بله | — |  |
| `sh_i_m` | int(4) | بله | — |  |
| `sharh` | nvarchar(5100) | بله | — |  |
| `vis_rdf` | int(4) | خیر | — |  |
| `user_d` | nvarchar(600) | بله | — |  |
| `defi_vis` | int(4) | بله | — |  |
| `hesab_status` | int(4) | خیر | — | ((1)) |
| `maxopen_time` | char(10) | بله | — |  |
| `check_eteb` | int(4) | بله | — |  |
| `just_naghdi` | int(4) | بله | — |  |
| `black_list` | int(4) | بله | — |  |
| `result_m` | nvarchar(2000) | خیر | — | ('ذکر نشده') |
| `c_egh` | nvarchar(200) | بله | — |  |
| `c_mel` | nvarchar(200) | بله | — |  |
| `c_pos` | nvarchar(200) | بله | — |  |
| `kind` | int(4) | بله | — |  |
| `IsEmp` | int(4) | بله | — | ((0)) |
| `MaxManFactor` | int(4) | بله | — |  |
| `RDF_masir` | int(4) | بله | — |  |
| `Lat` | float(8) | بله | — |  |
| `Lng` | float(8) | بله | — |  |
| `TafsilCode` | nvarchar(12) | بله | — | ((-1)) |
| `Ecode_Vis` | int(4) | بله | — |  |
| `PersonalityType` | int(4) | بله | — |  |
| `EtehadieID` | int(4) | بله | — |  |
| `Shenaseh_Egh` | nvarchar(200) | بله | — |  |
| `TafsilID` | bigint(8) | بله | — |  |
| `Username` | nvarchar(200) | بله | — | ('-') |
| `Password` | nvarchar(1000) | بله | — | ('-') |
| `PriceCheck` | decimal(9) | بله | — |  |
| `CheckDateDay` | int(4) | بله | — |  |
| `ShmoMoaref` | int(4) | بله | — |  |
| `RoleCode` | nvarchar(40) | بله | — |  |
| `TransferCode` | nvarchar(100) | بله | — |  |
| `CustomerTypeTtmsId` | bigint(8) | بله | — |  |
| `CustomerBranch` | nvarchar(100) | بله | — | (NULL) |
| `TaxInvoiceType` | int(4) | خیر | — | ((1)) |
| `WithTax` | bit(1) | خیر | — | ((0)) |
| `FatherName` | nvarchar(100) | بله | — | (NULL) |
| `DateOfBirth` | nvarchar(20) | بله | — | (NULL) |
| `BusinessCategoryId` | int(4) | بله | — |  |

### `CallLog_Voip` — 0 ردیف (تخمین)
- کلیدها: PK_CallLog_Voip(PK)=RowID
- FK `FK_CallLog_Voip_CUSTOMERS`: shmo → CUSTOMERS.SHMO

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `userID` | nchar(20) | بله | — |  |
| `shmo` | int(4) | بله | — |  |
| `callType` | int(4) | بله | — |  |
| `callerNumber` | nvarchar(100) | بله | — |  |
| `respondentNumber` | nvarchar(100) | بله | — |  |
| `callDate` | nvarchar(20) | بله | — |  |
| `callTime` | nvarchar(16) | بله | — |  |
| `callEngDateTime` | datetime(8) | بله | — |  |
| `callTimerStr` | nvarchar(16) | بله | — |  |
| `callTimer` | bigint(8) | بله | — |  |
| `addressFile` | nchar(20) | بله | — |  |
| `taskID` | int(4) | بله | — |  |

### `CellNumberShopFactor` — 0 ردیف (تخمین)
- کلیدها: PK_CellNumberShopFactor(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `CellNumber` | nvarchar(100) | خیر | — |  |

### `ChangeStateFactorForoshTemp` — 0 ردیف (تخمین)
- کلیدها: <text 29>(PK)=FactorNumber

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `FactorNumber` | bigint(8) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `sysID` | int(4) | خیر | — |  |

### `ChangeStateFactorKharidTemp` — 0 ردیف (تخمین)
- کلیدها: <text 29>(PK)=FactorNumber

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `FactorNumber` | bigint(8) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `CheckTypes` — 2 ردیف (تخمین)
- کلیدها: PK_CheckTypes(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Desciption` | nvarchar(200) | خیر | — |  |

### `Checks` — 7 ردیف (تخمین)
- کلیدها: PK_Checks(PK)=CheckId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `CheckId` | int(4) | خیر | بله |  |
| `CheckName` | varchar(50) | خیر | — |  |
| `CheckAddress` | varchar(50) | خیر | — |  |

### `Cities` — 1165 ردیف (تخمین)
- کلیدها: PK_Cities(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `CityCode` | nvarchar(300) | بله | — |  |
| `CityName` | nvarchar(300) | خیر | — |  |
| `Description` | nvarchar(1000) | بله | — |  |

### `Company` — 1 ردیف (تخمین)
- کلیدها: PK_Company(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `name` | varchar(100) | خیر | — |  |
| `modir_amel` | varchar(50) | خیر | — |  |
| `rdf` | int(4) | خیر | بله |  |
| `current_` | char(1) | خیر | — |  |
| `addre` | text(16) | بله | — |  |
| `tell1` | varchar(50) | بله | — |  |
| `fax` | varchar(50) | بله | — |  |
| `cell` | varchar(50) | بله | — |  |
| `tell2` | varchar(50) | بله | — |  |
| `arm` | image(16) | بله | — |  |
| `date__` | char(10) | بله | — |  |
| `t_kind` | int(4) | بله | — |  |
| `C_meli` | varchar(50) | بله | — |  |
| `C_egh` | varchar(50) | بله | — |  |
| `C_pos` | varchar(50) | بله | — |  |
| `foroshgahi_active` | int(4) | بله | — |  |
| `TaxMemoryID` | nvarchar(100) | بله | — |  |
| `Branch` | nvarchar(100) | بله | — | (NULL) |
| `TaxPrivateKey` | nvarchar(-1) | بله | — |  |
| `CardNumber` | nvarchar(100) | بله | — |  |
| `ShabaNumber` | nvarchar(100) | بله | — |  |

### `ConfirmUser` — 968 ردیف (تخمین)
- کلیدها: PK_ConfirmUser(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `RealUserID` | int(4) | خیر | — |  |
| `RealUserName` | nvarchar(1000) | خیر | — |  |
| `FakeUserID` | int(4) | خیر | — |  |
| `FakeUserName` | nchar(1000) | خیر | — |  |
| `P` | bit(1) | خیر | — |  |
| `SumMab` | money(8) | خیر | — |  |
| `SumPos` | money(8) | خیر | — |  |
| `SumCheck` | money(8) | خیر | — |  |
| `Shmo` | bigint(8) | خیر | — |  |
| `Moname` | nvarchar(1000) | خیر | — |  |
| `RealPlusFake` | nvarchar(1000) | خیر | — |  |
| `Ghno` | int(4) | خیر | — |  |

### `ContactType` — 0 ردیف (تخمین)
- کلیدها: PK_ContactType(PK)=ContactID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ContactID` | int(4) | خیر | بله |  |
| `ContactName` | nvarchar(100) | بله | — |  |

### `Contradiction` — 0 ردیف (تخمین)
- کلیدها: PK_Contradiction(PK)=ContradictionID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ContradictionID` | int(4) | خیر | بله |  |
| `ContradictionTableNameID` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `DateClient` | nvarchar(20) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(100) | خیر | — |  |
| `ForeignID` | bigint(8) | خیر | — |  |

### `ContradictionTableName` — 3 ردیف (تخمین)
- کلیدها: PK_ContradictionTableName(PK)=ContradictionTableNameID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ContradictionTableNameID` | int(4) | خیر | — |  |
| `TableName` | nvarchar(1000) | خیر | — |  |

### `ControlColors` — 4 ردیف (تخمین)
- کلیدها: PK_ControlColors(PK)=ControlColorID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ControlColorID` | int(4) | خیر | — |  |
| `ControlColorName` | nvarchar(1000) | خیر | — |  |
| `PersianName` | nvarchar(1000) | خیر | — |  |

### `ControlNames` — 3 ردیف (تخمین)
- کلیدها: PK_ControlNames(PK)=ControlID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ControlID` | int(4) | خیر | — |  |
| `ControlName` | nvarchar(1000) | خیر | — |  |
| `PersianName` | nvarchar(1000) | خیر | — |  |

### `ConventionLog` — 0 ردیف (تخمین)
- کلیدها: PK_ConventionLog(PK)=conventionLog
- FK `FK_ConventionLog_companyTask`: toCompanyID → companyTask.CompanyID
- FK `FK_ConventionLog_convention`: conventionID → convention.conventionID
- FK `<text 35>`: fromConventionDurationID → conventionDuration.conventionDurationID
- FK `<text 36>`: toConventionDurationID → conventionDuration.conventionDurationID
- FK `<text 30>`: fromCompanyID → companyTask.CompanyID
- FK `<text 31>`: fromConventionTypeID → conventionType.conventionTypeID
- FK `<text 32>`: toConventionTypeID → conventionType.conventionTypeID
- FK `FK_ConventionLog_CUSTOMERS`: fromCustomerID → CUSTOMERS.SHMO
- FK `FK_ConventionLog_CUSTOMERS1`: toCustomerID → CUSTOMERS.SHMO
- FK `FK_ConventionLog_product`: fromProductID → product.productID
- FK `FK_ConventionLog_product1`: toProductID → product.productID
- FK `FK_ConventionLog_user`: fromAgentUserID → user.UserID
- FK `FK_ConventionLog_user1`: toAgentUserID → user.UserID
- FK `FK_ConventionLog_user2`: regUserID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `conventionLog` | int(4) | خیر | بله |  |
| `conventionID` | int(4) | بله | — |  |
| `fromCompanyID` | int(4) | بله | — |  |
| `toCompanyID` | int(4) | بله | — |  |
| `fromAgentUserID` | int(4) | بله | — |  |
| `toAgentUserID` | int(4) | بله | — |  |
| `fromCustomerID` | int(4) | بله | — |  |
| `toCustomerID` | int(4) | بله | — |  |
| `fromProductID` | int(4) | بله | — |  |
| `toProductID` | int(4) | بله | — |  |
| `fromConventionTypeID` | int(4) | بله | — |  |
| `toConventionTypeID` | int(4) | بله | — |  |
| `fromRegDate` | nvarchar(20) | بله | — |  |
| `toRegDate` | nvarchar(20) | بله | — |  |
| `fromStartDate` | nvarchar(20) | بله | — |  |
| `toStartDate` | nvarchar(20) | بله | — |  |
| `fromCountOfSystem` | int(4) | بله | — |  |
| `toCountOfSystem` | int(4) | بله | — |  |
| `fromComment` | nvarchar(-1) | بله | — |  |
| `toComment` | nvarchar(-1) | بله | — |  |
| `regUserID` | int(4) | بله | — |  |
| `fromConventionDurationID` | int(4) | بله | — |  |
| `toConventionDurationID` | int(4) | بله | — |  |
| `fromPrice` | decimal(9) | بله | — |  |
| `toPrice` | decimal(9) | بله | — |  |

### `ConventionProductProperty` — 5 ردیف (تخمین)
- کلیدها: PK_ConventionProductProperty(PK)=ConventionProductPropertyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ConventionProductPropertyID` | int(4) | خیر | بله |  |
| `ConventionID` | int(4) | بله | — |  |
| `ProductID` | int(4) | بله | — |  |
| `PropertyID` | int(4) | بله | — |  |

### `CooperationTypes` — 3 ردیف (تخمین)
- کلیدها: PK_CooperationTypes(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `CooperationType` | nvarchar(100) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |

### `Country` — 117 ردیف (تخمین)
- کلیدها: PK_Country(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `CountryCode` | nvarchar(200) | خیر | — |  |
| `CountryName` | nvarchar(400) | خیر | — |  |
| `Description` | nvarchar(1000) | بله | — |  |

### `CustomerClubScore` — 0 ردیف (تخمین)
- کلیدها: PK_CustomerClubScore(PK)=RowId
- FK `<text 30>`: Shmo → CUSTOMERS.SHMO
- FK `<text 37>`: Kind → KindCustomerClub.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | bigint(8) | خیر | بله |  |
| `Score` | int(4) | خیر | — |  |
| `Date` | char(10) | خیر | — |  |
| `Kind` | int(4) | خیر | — |  |
| `RefNum` | bigint(8) | خیر | — |  |
| `Shmo` | int(4) | خیر | — |  |
| `Desc` | nvarchar(2000) | خیر | — |  |
| `UserId` | int(4) | بله | — |  |

### `CustomerClubScoreDefinition` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=Id
- FK `<text 47>`: Kind → KindCustomerClub.Id
- FK `<text 39>`: SysId → osystems.rdf_system
- FK `<text 40>`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `Kind` | int(4) | خیر | — |  |
| `Date` | char(10) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |
| `Price` | money(8) | خیر | — |  |
| `Score` | int(4) | خیر | — |  |
| `FromDate` | char(10) | خیر | — |  |
| `ToDate` | char(10) | خیر | — |  |
| `SysId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |

### `CustomerInfo` — 0 ردیف (تخمین)
- کلیدها: PK_CustomerInfo(PK)=shmo
- FK `FK_CustomerInfo_CUSTOMERS`: shmo → CUSTOMERS.SHMO

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shmo` | int(4) | خیر | — |  |
| `firstName` | nvarchar(200) | بله | — |  |
| `LastName` | nvarchar(200) | بله | — |  |
| `companyName` | nvarchar(300) | بله | — |  |
| `birthday` | nvarchar(20) | بله | — |  |
| `marital` | bit(1) | بله | — |  |
| `weddingday` | nvarchar(20) | بله | — |  |
| `email1` | nvarchar(200) | بله | — |  |
| `email2` | nvarchar(200) | بله | — |  |
| `website` | nvarchar(200) | بله | — |  |
| `jobType` | nvarchar(1000) | بله | — |  |
| `instaID` | nvarchar(2000) | بله | — |  |
| `telegramID` | nvarchar(2000) | بله | — |  |

### `CustomerSite` — 0 ردیف (تخمین)
- کلیدها: PK_CostomerSite(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `FirstName` | nvarchar(382) | خیر | — |  |
| `LastName` | nvarchar(382) | خیر | — |  |
| `Cell` | nvarchar(382) | خیر | — |  |
| `CodeMeli` | char(10) | خیر | — |  |
| `SiteID` | bigint(8) | خیر | — |  |
| `shmo` | int(4) | بله | — |  |
| `DateDone` | char(10) | بله | — |  |

### `CustomerTablets` — 0 ردیف (تخمین)
- کلیدها: PK_CustomerTablets(PK)=cusotmerTabletID
- FK `FK_CustomerTablets_CUSTOMERS`: SHMO → CUSTOMERS.SHMO

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `cusotmerTabletID` | int(4) | خیر | بله |  |
| `androidVersion` | nvarchar(40) | بله | — |  |
| `cupID` | nvarchar(60) | بله | — |  |
| `activationCode` | nvarchar(60) | بله | — |  |
| `mark` | nvarchar(60) | بله | — |  |
| `model` | nvarchar(60) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |
| `creationDate` | nvarchar(20) | بله | — |  |
| `regUserID` | int(4) | بله | — |  |
| `SHMO` | int(4) | بله | — |  |

### `CustomerTypeTTMS` — 4 ردیف (تخمین)
- کلیدها: PK_CustomerTypeTTMS(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | — |  |
| `Title` | nvarchar(1000) | خیر | — |  |

### `DailyLeaveType` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Type` | nvarchar(200) | خیر | — |  |

### `DaryaftMultiFactor` — 0 ردیف (تخمین)
- کلیدها: PK_DaryaftMultiFactor(PK)=GhnoDar, Rdf_, Shfacfo

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GhnoDar` | int(4) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `Shfacfo` | bigint(8) | خیر | — |  |
| `Price` | decimal(9) | خیر | — |  |
| `Tafif` | decimal(9) | خیر | — |  |
| `IsTasvieh` | bit(1) | خیر | — | ((0)) |

### `DaryaftVaPardakht` — 0 ردیف (تخمین)
- کلیدها: PK_DaryaftVaPardakht(PK)=Ghno, Rdf_, IsDaryaft
- FK `FK_DaryaftVaPardakht_Moein`: MoeinId → Moein.MoeinID
- FK `<text 30>`: UserID → sys_users.user_id
- FK `FK_DaryaftVaPardakht_Tafsil`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Ghno` | int(4) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `IsDaryaft` | bit(1) | خیر | — |  |
| `Naghd` | decimal(9) | خیر | — |  |
| `SumCheck` | decimal(9) | خیر | — |  |
| `SumPos` | decimal(9) | خیر | — |  |
| `DateClient` | nvarchar(20) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(100) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `EbtalDis` | nvarchar(1000) | بله | — |  |
| `UserIDEbtal` | int(4) | بله | — |  |
| `DateEbtal` | nvarchar(20) | بله | — |  |
| `DateEbtalServer` | nvarchar(20) | بله | — |  |
| `TimeEbtalServer` | nvarchar(100) | بله | — |  |
| `TafifRiali` | decimal(9) | خیر | — |  |
| `TafsilID` | bigint(8) | بله | — |  |
| `MoeinId` | bigint(8) | خیر | — |  |

### `Day` — 365 ردیف (تخمین)
- کلیدها: PK_Day(PK)=DayID
- FK `FK_Day_Week`: WeekID → Week.WeekID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `DayID` | int(4) | خیر | بله |  |
| `Date` | nvarchar(100) | بله | — |  |
| `WeekID` | int(4) | خیر | — |  |
| `Name` | nvarchar(100) | بله | — |  |
| `IsHoliday` | int(4) | بله | — | ((0)) |
| `Description` | nvarchar(2000) | بله | — |  |
| `DaysOfWeekNumber` | int(4) | بله | — | ((0)) |

### `DefaultReports` — 234 ردیف (تخمین)
- کلیدها: PK_DefaultReports(PK)=ReportID
- FK `<text 29>`: ReportID → ReportsKind.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ReportID` | int(4) | خیر | — |  |
| `ReportContent` | varbinary(-1) | خیر | — |  |

### `DeleteEkhtetamieHistory` — 0 ردیف (تخمین)
- کلیدها: PK_DeleteEkhtetamieHistory(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `UserName` | nvarchar(400) | خیر | — |  |
| `SourceDatabase` | nvarchar(400) | خیر | — |  |
| `TargetDatabase` | nvarchar(400) | خیر | — |  |
| `Date` | nvarchar(20) | خیر | — |  |

### `DetailsOrderSite` — 0 ردیف (تخمین)
- کلیدها: PK_DetailsOrderSite(PK)=RowID
- FK `<text 35>`: OrderID → HeaderOrderSite.RowID
- FK `<text 29>`: Shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `OrderID` | bigint(8) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `Count` | int(4) | خیر | — |  |
| `Price` | bigint(8) | خیر | — |  |
| `Offer` | int(4) | خیر | — |  |

### `DetailsPricing` — 0 ردیف (تخمین)
- کلیدها: PK_DetailsPricing(PK)=RowID
- FK `<text 31>`: HeaderId → HeaderPricing.RowID
- FK `FK_DetailsPricing_inventory`: Shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `HeaderId` | int(4) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `PriceFinished` | money(8) | خیر | — |  |

### `Device` — 5 ردیف (تخمین)
- کلیدها: PK_Device(PK)=DeviceID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `DeviceID` | int(4) | خیر | بله |  |
| `ProductKey` | nvarchar(100) | خیر | — |  |
| `CPUID` | nvarchar(100) | بله | — |  |
| `Status` | int(4) | بله | — |  |
| `ActivationDate` | nchar(20) | بله | — |  |
| `ExpirationDate` | nchar(20) | بله | — |  |
| `SysID` | int(4) | بله | — |  |
| `DeviceType` | int(4) | بله | — |  |
| `DeviceName` | nvarchar(100) | بله | — |  |
| `AppID` | nvarchar(1000) | بله | — |  |

### `DeviceDistribution` — 0 ردیف (تخمین)
- کلیدها: PK_deviceAnbar(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ActivationCode` | nvarchar(100) | خیر | — |  |
| `CupId` | nvarchar(100) | خیر | — |  |
| `Status` | int(4) | خیر | — |  |
| `ActivationDate` | char(10) | خیر | — |  |
| `ExpirationDate` | char(10) | خیر | — |  |
| `DeviceName` | nvarchar(100) | خیر | — |  |
| `AppId` | nvarchar(1000) | خیر | — |  |

### `DeviceLocation` — 94 ردیف (تخمین)
- کلیدها: PK_DeviceLocation_1(PK)=ID
- FK `FK_DeviceLocation_Device`: DeviceID → Device.DeviceID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `DeviceID` | int(4) | خیر | — |  |
| `DateAndTime` | bigint(8) | خیر | — |  |
| `Latitude` | float(8) | خیر | — |  |
| `Longitude` | float(8) | خیر | — |  |
| `Angle` | smallint(2) | بله | — | ((0)) |
| `Speed` | float(8) | بله | — | ((0)) |

### `DeviceLocationDistribution` — 0 ردیف (تخمین)
- کلیدها: <text 29>(PK)=ID
- FK `<text 48>`: DeviceID → DeviceDistribution.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `DeviceID` | int(4) | خیر | — |  |
| `Date` | char(10) | خیر | — |  |
| `Time` | char(8) | خیر | — |  |
| `Lat` | float(8) | خیر | — |  |
| `Lng` | float(8) | خیر | — |  |

### `DeviceMessages` — 0 ردیف (تخمین)
- کلیدها: PK_DeviceMessages(PK)=RowID
- FK `FK_DeviceMessages_visitors`: VisID → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `VisID` | int(4) | خیر | — |  |
| `ReadTime` | datetime(8) | بله | — |  |
| `ReadDate` | nvarchar(20) | بله | — |  |
| `SendTime` | datetime(8) | خیر | — |  |
| `SendDate` | nvarchar(20) | خیر | — |  |
| `Message` | nvarchar(-1) | خیر | — |  |
| `Title` | nvarchar(1000) | بله | — |  |
| `ForcedUpdate` | bit(1) | بله | — | ((0)) |

### `DeviceSettingDistribution` — 0 ردیف (تخمین)
- کلیدها: PK_DeviceSettingDistribution(PK)=rowId
- FK `<text 37>`: VisRdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rowId` | int(4) | خیر | بله |  |
| `BankIdPosPDA` | int(4) | خیر | — |  |
| `BankIdPos` | nvarchar(1000) | خیر | — |  |
| `SysId` | nvarchar(100) | خیر | — |  |
| `SendLoc` | bit(1) | خیر | — |  |
| `VisRdf` | int(4) | خیر | — |  |

### `DeviceSettings` — 5 ردیف (تخمین)
- کلیدها: DeviceSettings_pk(PK)=ID
- FK `<text 34>`: VisitorId → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `VisitorId` | int(4) | خیر | — |  |
| `OwnersList` | nvarchar(-1) | بله | — |  |
| `LinesList` | nvarchar(-1) | بله | — |  |
| `DefaultServer` | nvarchar(-1) | بله | — |  |
| `DefaultPriceGrp` | int(4) | خیر | — | ((1)) |
| `VisitRangeLimit` | int(4) | خیر | — | ((0)) |
| `MojoodiType` | int(4) | خیر | — | ((0)) |
| `PrintCount` | int(4) | خیر | — | ((1)) |
| `ActsLimit` | int(4) | خیر | — | ((20)) |
| `TrackingTimeDiff` | int(4) | خیر | — | ((300)) |
| `TrackingDisplacementDiff` | int(4) | خیر | — | ((50)) |
| `NewVis4NoLoc` | bit(1) | خیر | — | ((0)) |
| `VisitHasLocation` | bit(1) | خیر | — | ((0)) |
| `DiscountApply` | bit(1) | خیر | — | ((0)) |
| `JozChange` | bit(1) | خیر | — | ((0)) |
| `SendLoc` | bit(1) | خیر | — | ((1)) |
| `VisitorSeeIP` | bit(1) | خیر | — | ((1)) |
| `HasAccessPishDaryaft` | bit(1) | خیر | — | ((0)) |
| `HasAccessPishFactor` | bit(1) | خیر | — | ((0)) |
| `HasAccessRahyab` | bit(1) | خیر | — | ((0)) |
| `ServerNamesList` | nvarchar(-1) | بله | — |  |
| `ServersList` | nvarchar(-1) | خیر | — |  |
| `PriceGrpType` | int(4) | خیر | — | ((0)) |
| `InventoryList` | nvarchar(-1) | بله | — |  |
| `RoozMasir` | bit(1) | خیر | — | ((0)) |
| `MaxAllowedSyncDays` | int(4) | خیر | — | ((1)) |
| `SelectByMasir` | bit(1) | خیر | — | ((0)) |
| `VisitSendTimeLimit` | int(4) | خیر | — | ((1)) |
| `CheckingBouncedCheck` | bit(1) | خیر | — | ((0)) |
| `InventoryCheck` | int(4) | خیر | — | ((0)) |
| `AutoPrize` | bit(1) | خیر | — | ((0)) |
| `IsTaxActive` | bit(1) | خیر | — | ((0)) |
| `<text 29>` | bit(1) | خیر | — | ((0)) |
| `AccessedKalaGroup` | nvarchar(-1) | بله | — | (NULL) |
| `CheckCredit` | int(4) | بله | — |  |
| `CanDirectSale` | bit(1) | خیر | — | ((0)) |
| `CanDirectDaryaft` | bit(1) | خیر | — | ((0)) |
| `CanDirectSaleOnCredit` | bit(1) | خیر | — | ((0)) |

### `DeviceUser` — 0 ردیف (تخمین)
- کلیدها: PK_DeviceUser(PK)=Id
- FK `FK_DeviceUser_user`: UserId → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `UserId` | int(4) | خیر | — |  |
| `AppId` | varchar(-1) | خیر | — |  |
| `CpuId` | varchar(-1) | خیر | — |  |

### `DirectTasvieh` — 0 ردیف (تخمین)
- کلیدها: PK_DirectTasvieh(PK)=RowID
- FK `FK_DirectTasvieh_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `Shfacfo` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(100) | خیر | — |  |
| `SailFact` | bit(1) | خیر | — | ((1)) |

### `Document` — 0 ردیف (تخمین)
- کلیدها: PK_Document_1(PK)=DocID
- FK `FK_Document_MergeAccountTbl`: MergeID → MergeAccountTbl.MergeID
- FK `FK_Document_osystems`: SysId → osystems.rdf_system
- FK `FK_Document_sys_users`: Uid → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `DocID` | bigint(8) | خیر | بله |  |
| `DocNumber` | int(4) | خیر | — |  |
| `DocDesc` | nvarchar(1000) | بله | — |  |
| `Uid` | int(4) | بله | — |  |
| `RegDate` | varchar(10) | بله | — |  |
| `FinalDate` | varchar(10) | بله | — |  |
| `DoDate` | varchar(10) | بله | — |  |
| `Time` | time(5) | بله | — |  |
| `Active` | smallint(2) | بله | — |  |
| `Kind` | int(4) | بله | — |  |
| `Shfac` | int(4) | بله | — |  |
| `MergeID` | int(4) | بله | — |  |
| `MainDocNumber` | int(4) | بله | — |  |
| `SysId` | int(4) | بله | — |  |
| `DeleteID` | bigint(8) | بله | — |  |

### `DocumentDetails` — 0 ردیف (تخمین)
- کلیدها: PK_DocumentDetails(PK)=RowID
- FK `FK_DocumentDetails_Document`: DocID → Document.DocID
- FK `FK_DocumentDetails_LevelFive`: LevelFiveId → LevelFive.LevelFiveId
- FK `FK_DocumentDetails_LevelSix`: LevelSixId → LevelSix.LevelSixId
- FK `FK_DocumentDetails_Moein`: MoeinId → Moein.MoeinID
- FK `FK_DocumentDetails_Tafsil`: TafsilCode → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `DocID` | bigint(8) | خیر | — |  |
| `Bed` | money(8) | بله | — |  |
| `Bes` | money(8) | بله | — |  |
| `Desc` | nvarchar(1000) | بله | — |  |
| `Active` | smallint(2) | بله | — |  |
| `TafsilCode` | bigint(8) | بله | — |  |
| `Kind` | int(4) | بله | — |  |
| `Shfac` | bigint(8) | بله | — |  |
| `LevelFiveId` | int(4) | بله | — |  |
| `LevelSixId` | int(4) | بله | — |  |
| `MoeinId` | bigint(8) | خیر | — |  |
| `kolName` | nvarchar(600) | خیر | — | ('') |
| `kolCode` | nvarchar(6) | خیر | — | ('') |
| `moeinName` | nvarchar(600) | خیر | — | ('') |
| `moeinCode` | nvarchar(6) | خیر | — | ('') |
| `tafsilName` | nvarchar(600) | بله | — |  |
| `codeTafsil` | nvarchar(12) | بله | — |  |

### `DocumentSourceKind` — 3 ردیف (تخمین)
- کلیدها: PK_DocumentSourceKind(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Description` | nvarchar(200) | خیر | — |  |

### `EditedReports` — 4 ردیف (تخمین)
- کلیدها: PK_EditedReports(PK)=RDF
- FK `FK_EditedReports_ReportsKind`: ReportID → ReportsKind.ID
- FK `FK_EditedReports_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | بله |  |
| `ReportID` | int(4) | خیر | — |  |
| `ReportContent` | varbinary(-1) | خیر | — |  |
| `UserID` | int(4) | بله | — |  |

### `EducationalDegree` — 13 ردیف (تخمین)
- کلیدها: PK_EducationalDegree(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Code` | nvarchar(100) | بله | — |  |
| `Degree` | nvarchar(100) | خیر | — |  |

### `EffectOfExternalCosts` — 2 ردیف (تخمین)
- کلیدها: PK_EffectOfExternalCosts(PK)=EffectID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `EffectID` | bit(1) | خیر | — |  |
| `Name` | nvarchar(1000) | خیر | — |  |

### `EmployeeStatus` — 7 ردیف (تخمین)
- کلیدها: PK_EmployeeStatus(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Status` | nvarchar(200) | خیر | — |  |

### `ErrorLog` — 0 ردیف (تخمین)
- کلیدها: PK_ErrorLog(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `ErrorDate` | date(3) | خیر | — |  |
| `ErrorShamsiDate` | nchar(20) | خیر | — |  |
| `ErrorTime` | time(5) | خیر | — |  |
| `UserID` | int(4) | بله | — |  |
| `PcName` | nvarchar(100) | بله | — |  |
| `PcIP` | nvarchar(100) | بله | — |  |
| `ErrorMessage` | text(16) | بله | — |  |
| `ErrorInnerExceptionMessage` | text(16) | بله | — |  |
| `Kind` | tinyint(1) | بله | — |  |

### `Ethadie` — 1 ردیف (تخمین)
- کلیدها: PK_Ethadie(PK)=RDF

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | بله |  |
| `Name` | nvarchar(2000) | خیر | — |  |

### `EventKinds` — 24 ردیف (تخمین)
- کلیدها: PK_EventKinds(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `KindName` | nvarchar(200) | بله | — |  |

### `EventManager` — 0 ردیف (تخمین)
- کلیدها: PK_EventManager(PK)=RowID
- FK `FK_EventManager_EventKinds`: KindID → EventKinds.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `MsID` | int(4) | خیر | — |  |
| `Shenaseh` | bigint(8) | خیر | — |  |
| `KindID` | int(4) | خیر | — |  |

### `EventStation` — 0 ردیف (تخمین)
- کلیدها: PK_EventStation(PK)=RowID
- FK `FK_EventStation_EventKinds`: KindID → EventKinds.ID
- FK `<text 32>`: SmID → StationManagment.SmID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `SmID` | int(4) | خیر | — |  |
| `Shenaseh` | bigint(8) | خیر | — |  |
| `KindID` | int(4) | خیر | — |  |

### `EventsTakhsisVisitor` — 1 ردیف (تخمین)
- کلیدها: PK_EventsTakhsisVisitor(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `VisitorRdf` | int(4) | خیر | — |  |
| `MasirRdf` | int(4) | خیر | — |  |
| `FromDate` | nvarchar(30) | بله | — |  |
| `ToDate` | nvarchar(30) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `DateSabt` | nvarchar(30) | بله | — |  |
| `Active` | nchar(20) | بله | — |  |

### `ExternalCosts` — 30 ردیف (تخمین)
- کلیدها: PK_ExternalCosts(PK)=RowID
- FK `<text 38>`: Effect → EffectOfExternalCosts.EffectID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(1000) | خیر | — |  |
| `Effect` | bit(1) | خیر | — |  |

### `ExternalEffectsDetails` — 0 ردیف (تخمین)
- کلیدها: PK_ExternalEffectsDetails(PK)=Shfacfo, ExternalRowID, Rdf_, ActType
- FK `<text 39>`: ExternalRowID → ExternalCosts.RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Shfacfo` | bigint(8) | خیر | — |  |
| `ExternalRowID` | int(4) | خیر | — |  |
| `Price` | decimal(9) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `ActType` | int(4) | خیر | — |  |

### `ExternalSourceDocuments` — 0 ردیف (تخمین)
- کلیدها: PK_ExternalSourceDocuments(PK)=RowID
- FK `<text 43>`: ConfirmUser → sys_users.user_id
- FK `<text 42>`: RejectUser → sys_users.user_id
- FK `<text 35>`: DocType → ActNames.ActID
- FK `<text 45>`: SourceID → DocumentSourceKind.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `DocID` | int(4) | خیر | — |  |
| `DocType` | int(4) | خیر | — |  |
| `DocPrice` | money(8) | خیر | — | ((0)) |
| `Date` | nvarchar(20) | خیر | — |  |
| `ConfirmUser` | int(4) | بله | — |  |
| `ConfirmDate` | nvarchar(20) | بله | — |  |
| `RejectUser` | int(4) | بله | — |  |
| `RejectDate` | nvarchar(20) | بله | — |  |
| `SourceID` | int(4) | خیر | — |  |

### `ExtraCustomers` — 0 ردیف (تخمین)
- کلیدها: PK_ExtraCustomers(PK)=rdf, shmo, vis_rdf, date
- FK `FK_ExtraCustomers_CUSTOMERS`: shmo → CUSTOMERS.SHMO
- FK `FK_ExtraCustomers_visitors`: vis_rdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `shmo` | int(4) | خیر | — |  |
| `vis_rdf` | int(4) | خیر | — |  |
| `date` | nvarchar(20) | خیر | — |  |

### `ExtraData` — 0 ردیف (تخمین)
- کلیدها: PK_ExtraData(PK)=ExtraDataId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ExtraDataId` | int(4) | خیر | بله |  |
| `FormId` | int(4) | خیر | — |  |
| `Name` | nvarchar(2000) | بله | — |  |
| `Value` | nvarchar(2000) | بله | — |  |

### `FactorConfirmation` — 0 ردیف (تخمین)
- کلیدها: PK_FactorConfirmation(PK)=Shfacfo

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Shfacfo` | bigint(8) | خیر | — |  |
| `Date` | nvarchar(20) | خیر | — |  |
| `UserName` | nvarchar(1000) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |

### `FactorType` — 4 ردیف (تخمین)
- کلیدها: PK_FactorType(PK)=factorTypeID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `factorTypeID` | int(4) | خیر | بله |  |
| `Type` | nvarchar(200) | بله | — |  |

### `Field` — 252 ردیف (تخمین)
- کلیدها: PK_Field(PK)=FieldId
- FK `FK_Field_Form`: FormId → Form.FormId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `FieldId` | int(4) | خیر | بله |  |
| `FormId` | int(4) | خیر | — |  |
| `Title` | nvarchar(2000) | خیر | — |  |
| `Description` | nvarchar(-1) | بله | — |  |
| `v1` | bit(1) | خیر | — | ((0)) |
| `v2` | bit(1) | خیر | — | ((0)) |
| `v3` | bit(1) | خیر | — | ((0)) |
| `v4` | bit(1) | خیر | — | ((0)) |
| `v5` | bit(1) | خیر | — | ((0)) |
| `v6` | bit(1) | خیر | — | ((0)) |
| `v7` | bit(1) | خیر | — | ((0)) |
| `v8` | bit(1) | خیر | — | ((0)) |
| `v9` | bit(1) | خیر | — | ((0)) |
| `v10` | bit(1) | خیر | — | ((0)) |
| `v11` | bit(1) | خیر | — | ((0)) |
| `v12` | bit(1) | خیر | — | ((0)) |
| `v13` | bit(1) | خیر | — | ((0)) |
| `v14` | bit(1) | خیر | — | ((0)) |
| `v15` | bit(1) | خیر | — | ((0)) |
| `v16` | bit(1) | خیر | — | ((0)) |
| `v17` | bit(1) | خیر | — | ((0)) |
| `v18` | bit(1) | خیر | — | ((0)) |
| `v19` | bit(1) | خیر | — | ((0)) |
| `v20` | bit(1) | خیر | — | ((0)) |

### `FileAttach` — 0 ردیف (تخمین)
- کلیدها: PK_FileAttach(PK)=Id
- FK `FK_FileAttach_FileAttachKind`: Kind → FileAttachKind.Id
- FK `FK_FileAttach_FileAttachType`: Type → FileAttachType.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | bigint(8) | خیر | بله |  |
| `Kind` | smallint(2) | خیر | — |  |
| `Type` | tinyint(1) | خیر | — |  |
| `Ref_Num` | bigint(8) | خیر | — |  |
| `Address` | nvarchar(1000) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `FileAttachKind` — 0 ردیف (تخمین)
- کلیدها: PK_FileAttachKind(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | smallint(2) | خیر | بله |  |
| `Name` | nvarchar(100) | خیر | — |  |

### `FileAttachType` — 0 ردیف (تخمین)
- کلیدها: PK_FileAttachType(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | tinyint(1) | خیر | بله |  |
| `Name` | nvarchar(100) | خیر | — |  |

### `Form` — 498 ردیف (تخمین)
- کلیدها: PK_Form(PK)=ID, FormId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `FormId` | int(4) | خیر | بله |  |
| `Title` | nvarchar(200) | خیر | — |  |
| `Title` | nvarchar(2000) | بله | — |  |
| `NameSpace` | nvarchar(2000) | بله | — |  |
| `Namespace` | nvarchar(200) | خیر | — |  |
| `Class` | nvarchar(100) | خیر | — |  |
| `Class` | nvarchar(2000) | بله | — |  |
| `Description` | nvarchar(2000) | بله | — |  |
| `v1` | bit(1) | بله | — |  |
| `v2` | bit(1) | بله | — |  |
| `v3` | bit(1) | بله | — |  |
| `v4` | bit(1) | بله | — |  |
| `v5` | bit(1) | بله | — |  |
| `v6` | bit(1) | بله | — |  |
| `v7` | bit(1) | بله | — |  |
| `v8` | bit(1) | بله | — |  |
| `v9` | bit(1) | بله | — |  |
| `v10` | bit(1) | بله | — |  |
| `v11` | bit(1) | بله | — | ((0)) |
| `v12` | bit(1) | بله | — | ((0)) |
| `v13` | bit(1) | بله | — | ((0)) |
| `v14` | bit(1) | بله | — | ((0)) |
| `v15` | bit(1) | بله | — | ((0)) |
| `v16` | bit(1) | بله | — | ((0)) |
| `v17` | bit(1) | بله | — | ((0)) |
| `v18` | bit(1) | بله | — | ((0)) |
| `v19` | bit(1) | بله | — | ((0)) |
| `v20` | bit(1) | بله | — | ((0)) |

### `Formulation` — 43 ردیف (تخمین)
- کلیدها: PK_Formulation(PK)=ID
- FK `FK_Formulation_inventory`: shka → inventory.shka
- FK `FK_Formulation_sys_users`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `Name` | nvarchar(200) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `Description` | nvarchar(2000) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |
| `DoneTime` | nvarchar(20) | خیر | — |  |

### `FormulationCost` — 40 ردیف (تخمین)
- کلیدها: PK_FormulationCost(PK)=ID
- FK `<text 30>`: FormulID → Formulation.ID
- FK `<text 31>`: CostID → IndirectCost.RowId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `FormulID` | bigint(8) | خیر | — |  |
| `CostID` | int(4) | خیر | — |  |
| `Price` | money(8) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `FormulationItems` — 66 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `<text 31>`: FormulID → Formulation.ID
- FK `<text 29>`: shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `FormulID` | bigint(8) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `TedadVahed` | decimal(9) | خیر | — |  |
| `TedadJoz` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `FromAtiranDocument` — 0 ردیف (تخمین)
- کلیدها: PK_FromAtiranDocument(PK)=DocID
- FK `<text 31>`: Uid → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `DocID` | bigint(8) | خیر | بله |  |
| `DocNumber` | int(4) | خیر | — |  |
| `DocDesc` | nvarchar(1000) | بله | — |  |
| `Uid` | int(4) | بله | — |  |
| `RegDate` | varchar(10) | بله | — |  |
| `FinalDate` | varchar(10) | بله | — |  |
| `DoDate` | varchar(10) | بله | — |  |
| `Time` | time(5) | بله | — |  |
| `Active` | smallint(2) | بله | — |  |
| `DocState` | smallint(2) | بله | — |  |
| `Stamp` | nvarchar(1000) | بله | — |  |
| `Kind` | int(4) | بله | — | ((0)) |

### `FromAtiranDocumentDetails` — 0 ردیف (تخمین)
- کلیدها: PK_FromAtiranDocumentDetails(PK)=RowID
- FK `<text 47>`: DocID → FromAtiranDocument.DocID
- FK `<text 34>`: MoeinId → Moein.MoeinID
- FK `<text 35>`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `DocID` | bigint(8) | خیر | — |  |
| `Bed` | money(8) | خیر | — | ((0)) |
| `Bes` | money(8) | خیر | — | ((0)) |
| `Desc` | nvarchar(1000) | بله | — |  |
| `Active` | smallint(2) | بله | — |  |
| `TafsilID` | bigint(8) | بله | — |  |
| `MoeinId` | bigint(8) | خیر | — |  |
| `kolName` | nvarchar(600) | خیر | — | ('') |
| `kolCode` | nvarchar(6) | خیر | — | ('') |
| `moeinName` | nvarchar(600) | خیر | — | ('') |
| `moeinCode` | nvarchar(6) | خیر | — | ('') |
| `tafsilName` | nvarchar(600) | بله | — |  |
| `codeTafsil` | nvarchar(12) | بله | — |  |

### `Gender` — 2 ردیف (تخمین)
- کلیدها: PK_Gender(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `GenderName` | nvarchar(100) | خیر | — |  |

### `GetActivationCode` — 0 ردیف (تخمین)
- کلیدها: PK_GetActivationCode(PK)=ID
- FK `<text 30>`: Shmo → CUSTOMERS.SHMO
- FK `<text 30>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Shmo` | int(4) | خیر | — |  |
| `RequestedCode` | nvarchar(100) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |

### `GetCheckHistoryStatus` — 25 ردیف (تخمین)
- کلیدها: PK_GetCheckHistoryStatus(PK)=GetStatusID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GetStatusID` | int(4) | خیر | — |  |
| `StatusName` | nvarchar(1000) | خیر | — |  |

### `GhabzSanad` — 87 ردیف (تخمین)
- کلیدها: PK_GhabzSanad(PK)=RowID
- FK `<text 32>`: DarDescriptionId → darDescriptionType.rowId
- FK `<text 53>`: <text 29> → <text 39>.rowId
- FK `FK_GhabzSanad_sys_users`: UserID → sys_users.user_id
- FK `<text 30>`: DeleteUserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `Date` | nvarchar(20) | خیر | — |  |
| `Time` | datetime(8) | خیر | — |  |
| `Ghno` | bigint(8) | خیر | — |  |
| `Mod` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `FromID` | bigint(8) | خیر | — |  |
| `ToID` | bigint(8) | بله | — |  |
| `Price` | money(8) | خیر | — |  |
| `Comment` | nvarchar(-1) | خیر | — |  |
| `FishNumber` | nvarchar(200) | بله | — |  |
| `CheckRdf` | int(4) | بله | — |  |
| `Active` | bit(1) | خیر | — |  |
| `KarMozd` | decimal(9) | بله | — |  |
| `DarDescriptionId` | bigint(8) | بله | — |  |
| `<text 29>` | bigint(8) | بله | — |  |
| `Sharh` | nvarchar(2000) | بله | — |  |
| `Shmo` | int(4) | بله | — |  |
| `DeleteUserId` | int(4) | بله | — |  |
| `IsModified` | bit(1) | خیر | — | ((0)) |
| `DoneDate` | nvarchar(20) | بله | — |  |
| `DeleteDate` | nvarchar(20) | بله | — |  |

### `GhabzSanadHistory` — 1 ردیف (تخمین)
- کلیدها: PK_GhabzSanadHistory(PK)=RowID, Rdf__, Mod
- FK `<text 30>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | — |  |
| `Rdf__` | int(4) | خیر | — |  |
| `Date` | nvarchar(20) | خیر | — |  |
| `Time` | datetime(8) | خیر | — |  |
| `Ghno` | bigint(8) | خیر | — |  |
| `Mod` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `FromID` | bigint(8) | خیر | — |  |
| `ToID` | bigint(8) | بله | — |  |
| `Price` | money(8) | خیر | — |  |
| `Comment` | nvarchar(-1) | خیر | — |  |
| `FishNumber` | nvarchar(200) | بله | — |  |
| `CheckRdf` | int(4) | بله | — |  |
| `Active` | bit(1) | خیر | — |  |
| `KarMozd` | decimal(9) | بله | — |  |
| `Shmo` | int(4) | بله | — |  |
| `Sharh` | nvarchar(2000) | بله | — |  |
| `DoneDate` | nvarchar(20) | بله | — |  |

### `GhabzSanadKind` — 25 ردیف (تخمین)
- کلیدها: PK_GhabzSanadKind(PK)=KindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `KindID` | int(4) | خیر | — |  |
| `KindName` | nvarchar(1000) | خیر | — |  |

### `GoodsActID` — 0 ردیف (تخمین)
- کلیدها: PK_GoodsActID(PK)=ActID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ActID` | int(4) | خیر | — |  |
| `ActIDName` | nvarchar(1000) | خیر | — |  |

### `GoodsKind` — 2 ردیف (تخمین)
- کلیدها: PK_GoodsKind(PK)=GoodsKindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GoodsKindID` | int(4) | خیر | — |  |
| `GoodsKindName` | nvarchar(1000) | خیر | — |  |

### `GroupLevelFive` — 0 ردیف (تخمین)
- کلیدها: PK_GroupLevelFive(PK)=RowId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `Code` | char(2) | خیر | — |  |
| `Name` | nvarchar(400) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `GroupLevelSix` — 0 ردیف (تخمین)
- کلیدها: PK_GroupLevelSix(PK)=RowId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `Code` | char(2) | خیر | — |  |
| `Name` | nvarchar(400) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `GroupMoeinTafsil` — 2 ردیف (تخمین)
- کلیدها: PK_MoeinGropuTafsil(PK)=ID
- FK `<text 31>`: GroupTafsilID → GroupTafsil.GroupID
- FK `FK_GroupMoeinTafsil_Moein`: MoeinID → Moein.MoeinID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `GroupTafsilID` | bigint(8) | خیر | — |  |
| `MoeinID` | bigint(8) | خیر | — |  |

### `GroupSarfasl` — 4 ردیف (تخمین)
- کلیدها: PK_GroupSarfasl(PK)=GroupSarfaslID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GroupSarfaslID` | int(4) | خیر | بله |  |
| `GroupSarfaslName` | nvarchar(1000) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `Kind` | int(4) | بله | — |  |

### `GroupSarfaslKind` — 5 ردیف (تخمین)
- کلیدها: PK_GroupSarfaslKind(PK)=GroupSarfaslKindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GroupSarfaslKindID` | int(4) | خیر | — |  |
| `GroupSarfaslKindName` | nvarchar(1000) | خیر | — |  |

### `GroupTafsil` — 10 ردیف (تخمین)
- کلیدها: PK_GroupTafsil(PK)=GroupID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GroupID` | bigint(8) | خیر | بله |  |
| `GroupName` | nvarchar(1000) | خیر | — |  |
| `Code` | nchar(6) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `IsEdit` | bit(1) | بله | — |  |

### `HIstoryKasr_e_Sanad` — 17 ردیف (تخمین)
- کلیدها: PK_HIstoryKasr_e_Sanad(PK)=rdf, kasr_e, ID
- FK `<text 32>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `rdf` | int(4) | خیر | — |  |
| `date_` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `total` | money(8) | خیر | — |  |
| `kasr_e` | int(4) | خیر | — |  |
| `sharh` | text(16) | خیر | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `UserID` | int(4) | خیر | — |  |
| `Hour` | char(8) | بله | — |  |
| `ComparisonDocNumber` | int(4) | بله | — |  |

### `HeaderOrderSite` — 0 ردیف (تخمین)
- کلیدها: PK_HeaderOrderSite(PK)=RowID
- FK `<text 31>`: CustomerSiteID → CustomerSite.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `OrderIDSite` | bigint(8) | خیر | — |  |
| `CustomerSiteID` | bigint(8) | خیر | — |  |
| `Mobile` | char(11) | خیر | — |  |
| `Tell` | nvarchar(100) | خیر | — |  |
| `Address` | nvarchar(382) | خیر | — |  |
| `RecipientName` | nvarchar(400) | خیر | — |  |
| `ZipCode` | char(10) | خیر | — |  |
| `Rent` | bigint(8) | خیر | — |  |
| `Offer` | bigint(8) | خیر | — |  |
| `Link` | nvarchar(400) | خیر | — |  |
| `Unicode` | nvarchar(400) | خیر | — |  |
| `DateDone` | char(10) | بله | — |  |

### `HeaderPricing` — 0 ردیف (تخمین)
- کلیدها: PK_HeaderPricing(PK)=RowID
- FK `FK_HeaderPricing_sys_users`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `Date` | char(10) | خیر | — |  |
| `DoneTime` | char(8) | خیر | — |  |
| `DoneDate` | char(10) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((0)) |

### `HelpTable` — 13 ردیف (تخمین)
- کلیدها: PK_HelpTable(PK)=NameTable, Column, Value

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `NameTable` | nvarchar(1000) | خیر | — |  |
| `Column` | nvarchar(1000) | خیر | — |  |
| `Value` | nvarchar(1000) | خیر | — |  |
| `Description` | nvarchar(-1) | بله | — |  |

### `HistoryAnbarSanad` — 0 ردیف (تخمین)
- کلیدها: PK_HistoryAnbarSanad(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | bigint(8) | خیر | بله |  |
| `IdAnbarSanad` | bigint(8) | خیر | — |  |
| `ShSanad` | bigint(8) | خیر | — |  |
| `ShFac` | bigint(8) | خیر | — |  |
| `Date` | char(10) | خیر | — |  |
| `Time` | char(10) | خیر | — |  |
| `desc` | nvarchar(1000) | خیر | — |  |
| `typeId` | int(4) | خیر | — |  |

### `HistoryFormulation` — 10 ردیف (تخمین)
- کلیدها: PK_HistoryFormulation(PK)=ID
- FK `<text 33>`: FormulID → Formulation.ID
- FK `<text 31>`: shka → inventory.shka
- FK `<text 31>`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `FormulID` | bigint(8) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `Name` | nvarchar(100) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `Description` | nvarchar(2000) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |
| `DoneTime` | nvarchar(20) | خیر | — |  |

### `HistoryFormulationCost` — 8 ردیف (تخمین)
- کلیدها: PK_HistoryFormulationCost(PK)=ID
- FK `<text 37>`: FormulID → Formulation.ID
- FK `<text 38>`: CostID → IndirectCost.RowId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `FormulID` | bigint(8) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `Price` | money(8) | خیر | — |  |
| `CostID` | int(4) | خیر | — |  |

### `HistoryFormulationItems` — 26 ردیف (تخمین)
- کلیدها: PK_HistoryFormulationItems(PK)=ID
- FK `<text 38>`: FormulID → Formulation.ID
- FK `<text 36>`: shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `FormulID` | bigint(8) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `TedadVahed` | decimal(9) | خیر | — |  |
| `TedadJoz` | int(4) | خیر | — |  |

### `HourlyLeaveType` — 2 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Type` | nvarchar(200) | خیر | — |  |

### `IPServer` — 1 ردیف (تخمین)
- کلیدها: PK_IPServer(PK)=ID
- FK `FK_IPServer_ServerKind`: Kind → IPServerKind.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(100) | خیر | — |  |
| `IP` | nvarchar(100) | خیر | — |  |
| `Kind` | int(4) | خیر | — |  |

### `IPServerKind` — 3 ردیف (تخمین)
- کلیدها: PK_ServerKind(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `Name` | nvarchar(100) | خیر | — |  |

### `IndirectCost` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=RowId
- FK `FK_IndirectCost_Moein`: MoeinID → Moein.MoeinID
- FK `FK_IndirectCost_Tafsil`: TafsilID → Tafsil.TafsilID
- FK `FK_IndirectCost_zirsarfasls`: RdfZirSarfasl → zirsarfasls.rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `CostName` | nvarchar(100) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `MoeinID` | bigint(8) | بله | — |  |
| `TafsilID` | bigint(8) | بله | — |  |
| `RdfZirSarfasl` | int(4) | بله | — |  |

### `Insurance` — 3 ردیف (تخمین)
- کلیدها: PK_Insurance(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `InsuranceName` | nvarchar(300) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |

### `InsuranceListD` — 0 ردیف (تخمین)
- کلیدها: PK_InsuranceListD(PK)=ID
- FK `<text 32>`: InsuranceListHID → InsuranceListH.ID
- FK `FK_Payslip_InsuranceListD`: PayslipID → Payslip.ID
- FK `FK_Personnel_InsuranceListD`: PersonnelID → Personnel.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `InsuranceListHID` | int(4) | خیر | — |  |
| `PayslipID` | int(4) | خیر | — |  |
| `DSW_ID` | nvarchar(40) | بله | — |  |
| `DSW_YY` | int(4) | بله | — |  |
| `DSW_MM` | int(4) | بله | — |  |
| `DSW_LISTNO` | nvarchar(100) | بله | — |  |
| `DSW_ID1` | nvarchar(100) | بله | — |  |
| `DSW_FNAME` | nvarchar(200) | بله | — |  |
| `DSW_LNAME` | nvarchar(200) | بله | — |  |
| `DSW_DNAME` | nvarchar(200) | بله | — |  |
| `DSW_IDNO` | nvarchar(40) | بله | — |  |
| `DSW_IDPLC` | nvarchar(200) | بله | — |  |
| `DSW_IDATE` | nvarchar(20) | بله | — |  |
| `DSW_BDATE` | nvarchar(20) | بله | — |  |
| `DSW_SEX` | nvarchar(20) | بله | — |  |
| `DSW_NAT` | nvarchar(20) | بله | — |  |
| `DSW_OCP` | nvarchar(200) | بله | — |  |
| `DSW_SDATE` | nvarchar(20) | بله | — |  |
| `DSW_EDATE` | nvarchar(20) | بله | — |  |
| `DSW_DD` | int(4) | بله | — |  |
| `DSW_ROOZ` | money(8) | بله | — |  |
| `DSW_MAH` | money(8) | بله | — |  |
| `DSW_MAZ` | money(8) | بله | — |  |
| `DSW_MASH` | money(8) | بله | — |  |
| `DSW_TOTL` | money(8) | بله | — |  |
| `DSW_BIME` | money(8) | بله | — |  |
| `DSW_PRATE` | money(8) | بله | — |  |
| `DSW_JOB` | nvarchar(100) | بله | — |  |
| `PER_NATCOD` | nvarchar(40) | بله | — |  |
| `DSK_ID` | nvarchar(40) | بله | — |  |
| `DSK_NAME` | nvarchar(200) | بله | — |  |
| `DSK_FARM` | nvarchar(200) | بله | — |  |
| `DSK_ADRS` | nvarchar(200) | بله | — |  |
| `DSK_KIND` | int(4) | بله | — |  |
| `DSK_YY` | int(4) | بله | — |  |
| `DSK_MM` | int(4) | بله | — |  |
| `DSK_LISTNO` | nvarchar(24) | بله | — |  |
| `DSK_DISK` | nvarchar(200) | بله | — |  |
| `DSK_NUM` | int(4) | بله | — |  |
| `DSK_TDD` | int(4) | بله | — |  |
| `DSK_TROOZ` | money(8) | بله | — |  |
| `DSK_TMAH` | money(8) | بله | — |  |
| `DSK_TMAZ` | money(8) | بله | — |  |
| `DSK_TMASH` | money(8) | بله | — |  |
| `DSK_TTOTL` | money(8) | بله | — |  |
| `DSK_TBIME` | money(8) | بله | — |  |
| `DSK_KOSO` | money(8) | بله | — |  |
| `DSK_BIC` | money(8) | بله | — |  |
| `DSK_RATE` | money(8) | بله | — |  |
| `DSK_PRATE` | money(8) | بله | — |  |
| `DSK_BIMH` | money(8) | بله | — |  |
| `DSK_PYM` | nvarchar(20) | بله | — |  |
| `PersonnelID` | int(4) | بله | — |  |

### `InsuranceListH` — 0 ردیف (تخمین)
- کلیدها: PK_InsuranceListH(PK)=ID
- FK `FK_InsuranceListH_sys_users`: InsertBy → sys_users.user_id
- FK `FK_InsuranceListH_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_Workhouse_InsuranceListH`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `Year` | int(4) | بله | — |  |
| `YearString` | nchar(20) | بله | — |  |
| `Month` | int(4) | بله | — |  |
| `ListNumber` | int(4) | بله | — |  |
| `Description` | nvarchar(600) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `InvantoryAnbar` — 0 ردیف (تخمین)
- کلیدها: PK_InvantoryAnbar_1(PK)=shka, rdfAnbar
- FK `FK_InvantoryAnbar_anbars`: rdfAnbar → anbars.rdf_anbar
- FK `FK_InvantoryAnbar_inventory`: shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka` | bigint(8) | خیر | — |  |
| `rdfAnbar` | int(4) | خیر | — |  |
| `vah` | decimal(9) | خیر | — |  |
| `joz` | int(4) | خیر | — |  |
| `tedBas` | int(4) | خیر | — |  |

### `InvantoryAnbarPS` — 0 ردیف (تخمین)
- کلیدها: PK_InvantoryAnbarPS(PK)=id
- FK `FK_InvantoryAnbarPS_anbars`: rdfAnbar → anbars.rdf_anbar
- FK `<text 29>`: shka → inventory.shka
- FK `<text 36>`: psId → ProductionSeries.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `shka` | bigint(8) | خیر | — |  |
| `rdfAnbar` | int(4) | خیر | — |  |
| `psId` | bigint(8) | خیر | — |  |
| `vah` | decimal(9) | خیر | — |  |
| `joz` | int(4) | خیر | — |  |
| `tedBas` | int(4) | خیر | — |  |

### `InventoryType` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `name` | nvarchar(60) | بله | — |  |

### `Inventory_Anbars_PS` — 1 ردیف (تخمین)
- کلیدها: PK_Inventory_Anbars_PS(PK)=Id
- FK `<text 29>`: rdfAnbar → anbars.rdf_anbar
- FK `<text 32>`: shka → inventory.shka
- FK `<text 39>`: PSId → ProductionSeries.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | bigint(8) | خیر | بله |  |
| `shka` | bigint(8) | خیر | — |  |
| `rdfAnbar` | int(4) | خیر | — |  |
| `PSId` | bigint(8) | خیر | — |  |
| `mojkavah` | decimal(9) | خیر | — |  |
| `mojkajoz` | int(4) | خیر | — |  |
| `TedBastebandi` | decimal(9) | خیر | — | ((0)) |

### `Inventory_Anbars_Variety` — 1803 ردیف (تخمین)
- کلیدها: PK_Inventory_Anbars_Variety(PK)=Shka, AnbarID, VarietyID
- FK `<text 34>`: AnbarID → anbars.rdf_anbar
- FK `<text 37>`: Shka → inventory.shka
- FK `<text 35>`: VarietyID → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `Shka` | bigint(8) | خیر | — |  |
| `AnbarID` | int(4) | خیر | — |  |
| `VarietyID` | int(4) | خیر | — |  |
| `Mojkavah` | decimal(9) | خیر | — |  |
| `Mojkajoz` | int(4) | خیر | — |  |

### `Inventory_coka` — 32 ردیف (تخمین)
- کلیدها: PK_Inventory_coka(PK)=KalaCode
- FK `FK_Inventory_coka_inventory`: shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka` | bigint(8) | خیر | — |  |
| `KalaCode` | nvarchar(200) | خیر | — |  |
| `Description` | nvarchar(1000) | بله | — |  |
| `IsCokaForJoz` | bit(1) | خیر | — | ((1)) |

### `InvoiceLimits` — 0 ردیف (تخمین)
- کلیدها: PK_InvoiceLimits(PK)=Id
- FK `FK_InvoiceLimits_LimitType`: LimitTypeId → LimitType.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `LimitTypeId` | int(4) | خیر | — |  |
| `CustomerGroupId` | int(4) | خیر | — | ((0)) |
| `ProvinceID` | int(4) | خیر | — | ((0)) |
| `CityId` | int(4) | خیر | — | ((0)) |
| `RegionId` | int(4) | خیر | — | ((0)) |
| `QuarterId` | int(4) | خیر | — | ((0)) |
| `PathId` | int(4) | خیر | — | ((0)) |
| `FromDate` | nvarchar(20) | خیر | — |  |
| `ToDate` | nvarchar(20) | خیر | — |  |
| `SumInvoiceAmount` | decimal(9) | خیر | — | ((0)) |
| `SumInvoiceLine` | int(4) | خیر | — | ((0)) |
| `SumInvoiceQuantity` | int(4) | خیر | — | ((0)) |
| `LineId` | int(4) | خیر | — | ((0)) |

### `InvoiceNumberCounter` — 1 ردیف (تخمین)
- کلیدها: <text 30>(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `LastNo` | int(4) | خیر | — |  |

### `IrTaxID` — 0 ردیف (تخمین)
- کلیدها: PK_IrTaxID(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | bigint(8) | خیر | بله |  |
| `ShFac` | bigint(8) | خیر | — |  |
| `IrTaxId` | nvarchar(100) | خیر | — |  |

### `Jobs` — 65533 ردیف (تخمین)
- کلیدها: PK__Jobs__3214EC275AFA6481(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `JobCode` | nvarchar(300) | خیر | — |  |
| `JobName` | nvarchar(600) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |

### `KalaTypeTTMS` — 12 ردیف (تخمین)
- کلیدها: PK_KalaTypeTTMS(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | — |  |
| `Title` | nvarchar(1000) | خیر | — |  |

### `KindCustomerClub` — 13 ردیف (تخمین)
- کلیدها: PK_KindCustomerClub(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `Decsription` | nvarchar(1000) | خیر | — |  |
| `FlagPositiveorNegative` | bit(1) | خیر | — |  |

### `KindName` — 56 ردیف (تخمین)
- کلیدها: PK_KindName(PK)=KindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `KindID` | int(4) | خیر | — |  |
| `KindName` | nvarchar(1000) | خیر | — |  |

### `KindZirSarfasl` — 0 ردیف (تخمین)

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `Name` | nvarchar(100) | خیر | — |  |

### `Kol` — 30 ردیف (تخمین)
- کلیدها: PK_Kol(PK)=KolID
- FK `FK_Kol_Grouh`: GrouhID → grouh.GrouhID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `KolID` | bigint(8) | خیر | بله |  |
| `KolCode` | nchar(6) | خیر | — |  |
| `Name` | nvarchar(2000) | خیر | — |  |
| `GrouhID` | bigint(8) | خیر | — |  |
| `CanDefineMoein` | bit(1) | بله | — |  |
| `IsEdit` | bit(1) | خیر | — | ((0)) |

### `Leave` — 0 ردیف (تخمین)
- کلیدها: PK__Leave__3214EC2716FBAD7F(PK)=ID
- FK `FK_Leave_DailyLeaveType`: DailyLeaveTypeID → DailyLeaveType.ID
- FK `FK_Leave_HourlyLeaveType`: HourlyLeaveTypeID → HourlyLeaveType.ID
- FK `FK_Leave_LeaveType`: LeaveTypeID → LeaveType.ID
- FK `FK_Leave_Personnel`: PersonnelID → Personnel.ID
- FK `FK_Leave_sys_users`: InsertBy → sys_users.user_id
- FK `FK_Leave_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_Leave_WorkhouseID`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `LeaveTypeID` | int(4) | خیر | — |  |
| `HourlyLeaveTypeID` | int(4) | بله | — |  |
| `DailyLeaveTypeID` | int(4) | بله | — |  |
| `ExitTime` | nchar(20) | بله | — |  |
| `ArrivalTime` | nchar(20) | بله | — |  |
| `HourlyLeaveDate` | datetime(8) | بله | — |  |
| `DailyLeaveStartDate` | datetime(8) | بله | — |  |
| `DailyLeaveEndDate` | datetime(8) | بله | — |  |
| `VacationDaysInDailyLeave` | int(4) | بله | — |  |
| `Description` | nvarchar(800) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `RequestDate` | datetime(8) | بله | — |  |
| `RequestShamsiDate` | nchar(20) | بله | — |  |
| `LeaveDays` | int(4) | بله | — |  |
| `HourlyLeaveShamsiDate` | nchar(20) | بله | — |  |
| `DailyLeaveStartShamsiDate` | nchar(20) | بله | — |  |
| `DailyLeaveEndShamsiDate` | nchar(20) | بله | — |  |
| `HourlyLeaveDuration` | bigint(8) | بله | — |  |
| `WorkhouseID` | int(4) | بله | — |  |
| `Confirmed` | bit(1) | بله | — |  |

### `LeaveType` — 2 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Type` | nvarchar(200) | خیر | — |  |

### `LevelFive` — 0 ردیف (تخمین)
- کلیدها: PK_LevelFive(PK)=LevelFiveId
- FK `FK_LevelFive_GroupLevelFive`: GroupId → GroupLevelFive.RowId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `LevelFiveId` | int(4) | خیر | بله |  |
| `GroupId` | int(4) | خیر | — |  |
| `LevelFiveCode` | char(4) | خیر | — |  |
| `Name` | nvarchar(400) | خیر | — |  |
| `FullCode` | char(6) | خیر | — |  |
| `FullName` | nvarchar(400) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `LevelSix` — 0 ردیف (تخمین)
- کلیدها: PK_LevelSix_1(PK)=LevelSixId
- FK `FK_LevelSix_GroupLevelSix`: GroupId → GroupLevelSix.RowId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `LevelSixId` | int(4) | خیر | بله |  |
| `GroupId` | int(4) | خیر | — |  |
| `LevelSixCode` | char(4) | خیر | — |  |
| `Name` | nvarchar(400) | خیر | — |  |
| `FullCode` | char(6) | خیر | — |  |
| `FullName` | nvarchar(400) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `LimitType` — 1 ردیف (تخمین)
- کلیدها: PK_LimitType(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `Description` | nvarchar(200) | خیر | — |  |

### `LimitedServices` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `methodName` | varchar(100) | خیر | — |  |
| `isLimit` | bit(1) | خیر | — | ((0)) |

### `Loan` — 0 ردیف (تخمین)
- کلیدها: PK_Loan(PK)=ID
- FK `FK_Loan_Personnel`: PersonnelID → Personnel.ID
- FK `FK_Loan_sys_users`: InsertBy → sys_users.user_id
- FK `FK_Loan_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_Loan_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `LoanPrice` | money(8) | خیر | — |  |
| `InstallmentCount` | smallint(2) | خیر | — |  |
| `StartDateInstallment` | date(3) | خیر | — |  |
| `InstallmentIntervals` | smallint(2) | خیر | — |  |
| `InstallmentPrice` | money(8) | خیر | — |  |
| `FirstInstallmentYear` | smallint(2) | بله | — |  |
| `FirstInstallmentMonth` | smallint(2) | بله | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `LoanInstallment` — 0 ردیف (تخمین)
- کلیدها: PK_LoanInstallment(PK)=ID
- FK `FK_LoanInstallment_Loan`: LoanID → Loan.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `LoanID` | int(4) | خیر | — |  |
| `PaymentDate` | date(3) | خیر | — |  |
| `Price` | money(8) | خیر | — |  |
| `Month` | smallint(2) | بله | — |  |
| `Year` | smallint(2) | بله | — |  |
| `IsPaid` | bit(1) | بله | — |  |

### `Log` — 1196 ردیف (تخمین)
- کلیدها: PK_Log(PK)=Id
- FK `FK_Log_sys_users`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `ComputerName` | nvarchar(100) | خیر | — |  |
| `ComputerIp` | nvarchar(100) | خیر | — |  |
| `ErrorText` | text(16) | خیر | — |  |
| `Date` | char(10) | خیر | — |  |
| `Date_G` | char(10) | خیر | — |  |
| `Time` | char(8) | خیر | — |  |
| `UserId` | int(4) | بله | — |  |
| `ErrorKindId` | smallint(2) | خیر | — |  |

### `LoginDetails` — 475 ردیف (تخمین)
- کلیدها: PK_LoginDetails(PK)=RowID
- FK `FK_LoginDetails_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `DateClient` | datetime(8) | خیر | — |  |
| `DateServer` | datetime(8) | خیر | — |  |
| `ComputerIP` | nvarchar(1000) | خیر | — |  |
| `ComputerName` | nvarchar(1000) | خیر | — |  |

### `Mac` — 0 ردیف (تخمین)
- کلیدها: PK_Mac(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `MacAddress` | nvarchar(100) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |

### `MaritalStatus` — 2 ردیف (تخمین)
- کلیدها: PK_MaritalStatus(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `StatusName` | nvarchar(40) | خیر | — |  |

### `MasirDay` — 0 ردیف (تخمین)
- کلیدها: PK_MasirDay(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Date_` | varchar(10) | خیر | — |  |
| `VisRdf` | int(4) | خیر | — |  |
| `CityRdf` | int(4) | خیر | — |  |
| `RegionRdf` | int(4) | خیر | — |  |
| `MasirRdf` | int(4) | خیر | — |  |
| `QuarterID` | int(4) | خیر | — | ((0)) |

### `MasirGoals` — 0 ردیف (تخمین)
- کلیدها: PK_MasirGoals(PK)=ID
- FK `FK_MasirGoals_CustGroup`: CustGroupRDF → custgroup.group_rdf
- FK `FK_MasirGoals_kagroup`: KaGroupRdf → kagroup.group_rdf
- FK `FK_MasirGoals_masir`: MasirRdf → masir.rdf_masir
- FK `FK_MasirGoals_sys_users`: UserID → sys_users.user_id
- FK `FK_MonthID`: MonthID → Month.MonthID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `MonthID` | int(4) | بله | — |  |
| `MasirRdf` | int(4) | بله | — |  |
| `KaGroupRdf` | int(4) | بله | — |  |
| `Tedadvahed` | bigint(8) | بله | — |  |
| `Mablagh` | money(8) | بله | — |  |
| `Date` | nvarchar(30) | بله | — |  |
| `time` | nvarchar(20) | بله | — |  |
| `CustGroupRDF` | int(4) | بله | — |  |
| `ActID` | int(4) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |

### `Menu` — 557 ردیف (تخمین)
- کلیدها: PK_Menu(PK)=MenuID
- FK `FK_Menu_Form`: FormID → Form.FormId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MenuID` | int(4) | خیر | بله |  |
| `SubSystemID` | int(4) | بله | — |  |
| `Text` | nvarchar(200) | بله | — |  |
| `Description` | nvarchar(200) | بله | — |  |
| `ParentMenuID` | int(4) | بله | — |  |
| `FormID` | int(4) | بله | — |  |
| `order` | int(4) | بله | — |  |
| `Shortcut` | nvarchar(-1) | بله | — |  |

### `MergeAccountTbl` — 0 ردیف (تخمین)
- کلیدها: PK_MergeAccountTbl(PK)=MergeID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MergeID` | int(4) | خیر | بله |  |
| `MergeNumber` | int(4) | خیر | — |  |
| `Explain` | nvarchar(1000) | خیر | — |  |
| `DateClient` | nvarchar(20) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `MainMergeNumber` | int(4) | بله | — |  |

### `MilitaryServiceSituation` — 3 ردیف (تخمین)
- کلیدها: PK_MilitaryServiceSituation(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `Situation` | nvarchar(100) | خیر | — |  |

### `Mission` — 0 ردیف (تخمین)
- کلیدها: PK_Mission(PK)=ID
- FK `FK_Mission_MissionType`: MissionTypeID → MissionType.ID
- FK `FK_Mission_Personnel`: PersonnelID → Personnel.ID
- FK `FK_Mission_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `MissionTypeID` | int(4) | خیر | — |  |
| `ShamsiFromDate` | nvarchar(100) | خیر | — |  |
| `FromDate` | datetime(8) | خیر | — |  |
| `ShamsiToDate` | nvarchar(100) | خیر | — |  |
| `ToDate` | datetime(8) | خیر | — |  |
| `MissionDays` | int(4) | خیر | — |  |
| `MissionTime` | nchar(20) | خیر | — |  |
| `Subject` | nvarchar(300) | خیر | — |  |
| `MissionLocation` | nvarchar(200) | خیر | — |  |
| `BeginningMission` | nvarchar(200) | خیر | — |  |
| `GoalMission` | nvarchar(200) | خیر | — |  |
| `MandateShamsiDate` | nvarchar(100) | خیر | — |  |
| `MandateDate` | datetime(8) | خیر | — |  |
| `Description` | nvarchar(800) | خیر | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `DailyMission` | bit(1) | بله | — |  |
| `ShiftID` | int(4) | بله | — |  |
| `ApprovedOvertime` | bit(1) | بله | — |  |
| `StartTime` | bigint(8) | بله | — |  |
| `EndTime` | bigint(8) | بله | — |  |
| `Confirmed` | bit(1) | بله | — |  |

### `MissionType` — 3 ردیف (تخمین)
- کلیدها: PK_MissionType(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `MissionTypeName` | nvarchar(100) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |

### `MixKala` — 0 ردیف (تخمین)
- کلیدها: PK_MixKala(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `PriceIn` | decimal(9) | خیر | — |  |
| `PriceOut` | decimal(9) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |

### `Moein` — 26 ردیف (تخمین)
- کلیدها: PK_Moein(PK)=MoeinID
- FK `FK_Moein_Kol`: KolID → Kol.KolID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MoeinID` | bigint(8) | خیر | بله |  |
| `MoeinCode` | nchar(6) | خیر | — |  |
| `Name` | nvarchar(2000) | خیر | — |  |
| `KolID` | bigint(8) | خیر | — |  |
| `FullName` | nvarchar(2000) | خیر | — |  |
| `FullCode` | nchar(16) | خیر | — |  |
| `IsEdit` | bit(1) | خیر | — | ((0)) |
| `AddToDoc` | bit(1) | بله | — |  |

### `MoeinToGroupLevelFive` — 0 ردیف (تخمین)
- کلیدها: PK_MoeinToGroupLevelFive(PK)=RowId
- FK `<text 39>`: GroupId → GroupLevelFive.RowId
- FK `<text 30>`: MoeinId → Moein.MoeinID
- FK `<text 34>`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `MoeinId` | bigint(8) | خیر | — |  |
| `GroupId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |

### `MoeinToGroupLevelSix` — 0 ردیف (تخمین)
- کلیدها: PK_MoeinToGroupLevelSix(PK)=RowId
- FK `<text 37>`: GroupId → GroupLevelSix.RowId
- FK `<text 29>`: MoeinId → Moein.MoeinID
- FK `<text 33>`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `MoeinId` | bigint(8) | خیر | — |  |
| `GroupId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |

### `MoeinToLevelFive` — 0 ردیف (تخمین)
- کلیدها: PK_MoeinToLevelFive(PK)=RowId
- FK `<text 29>`: LevelFiveId → LevelFive.LevelFiveId
- FK `FK_MoeinToLevelFive_Moein`: MoeinId → Moein.MoeinID
- FK `<text 29>`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `MoeinId` | bigint(8) | خیر | — |  |
| `LevelFiveId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |

### `MoeinToLevelSix` — 0 ردیف (تخمین)
- کلیدها: PK_MoeinToLevelSix(PK)=RowId
- FK `FK_MoeinToLevelSix_LevelSix`: LevelSixId → LevelSix.LevelSixId
- FK `FK_MoeinToLevelSix_Moein`: MoeinId → Moein.MoeinID
- FK `FK_MoeinToLevelSix_sys_users`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `MoeinId` | bigint(8) | خیر | — |  |
| `LevelSixId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |

### `Month` — 12 ردیف (تخمین)
- کلیدها: PK_Month(PK)=MonthID
- FK `FK_Month_Year`: YearID → Year.YearID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MonthID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(100) | بله | — |  |
| `YearID` | int(4) | خیر | — |  |

### `Months` — 12 ردیف (تخمین)
- کلیدها: PK_Months(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | smallint(2) | خیر | بله |  |
| `MonthName` | nvarchar(100) | خیر | — |  |

### `MultiCompanyAtiran` — 1 ردیف (تخمین)
- کلیدها: PK_MultiCopany(PK)=Rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Rdf` | int(4) | خیر | بله |  |
| `CompanyName` | nvarchar(1000) | خیر | — |  |
| `DataBaseName` | nvarchar(1000) | خیر | — |  |
| `InformationType` | int(4) | خیر | — |  |

### `NBank` — 188 ردیف (تخمین)
- کلیدها: PK_NBank(PK)=NBankID, NDate
- FK `FK_NBank_BANK`: NBankID → BANK.RDF

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `NBankID` | int(4) | خیر | — |  |
| `NMan` | money(8) | بله | — |  |
| `NDate` | nvarchar(20) | خیر | — |  |

### `NCow` — 28 ردیف (تخمین)
- کلیدها: PK_NCow(PK)=NDate

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `NMan` | money(8) | بله | — |  |
| `NDate` | nvarchar(20) | خیر | — |  |

### `NCustomers` — 16339 ردیف (تخمین)
- کلیدها: PK_NCustomers(PK)=NShmo, NDate
- FK `FK_NCustomers_NCustomers`: NShmo → CUSTOMERS.SHMO

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `NShmo` | int(4) | خیر | — |  |
| `NMan` | money(8) | بله | — |  |
| `NDate` | nvarchar(20) | خیر | — |  |
| `BlackList` | bit(1) | خیر | — | ((0)) |

### `NGetchk` — 568 ردیف (تخمین)
- کلیدها: PK_NGetchk(PK)=NRdf, NDate
- FK `FK_NGetchk_getchk`: NRdf → getchk.rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `NRdf` | bigint(8) | خیر | — |  |
| `Ngetchkmab` | money(8) | بله | — |  |
| `Nchk_satus` | int(4) | بله | — |  |
| `NDate` | nvarchar(20) | خیر | — |  |

### `NPutchk` — 1530 ردیف (تخمین)
- کلیدها: PK_NPutchk(PK)=NRdf, NDate
- FK `FK_NPutchk_putchk`: NRdf → putchk.rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `NRdf` | bigint(8) | خیر | — |  |
| `Nputchkmab` | money(8) | بله | — |  |
| `NDate` | nvarchar(20) | خیر | — |  |

### `NZirSarfasls` — 28 ردیف (تخمین)
- کلیدها: PK_NZirSarfasls(PK)=NZirSarfaslsID, NDate
- FK `FK_NZirSarfasls_zirsarfasls`: NZirSarfaslsID → zirsarfasls.rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `NZirSarfaslsID` | int(4) | خیر | — |  |
| `NBed` | money(8) | بله | — |  |
| `NBes` | money(8) | بله | — |  |
| `NDate` | nvarchar(20) | خیر | — |  |

### `Nationality` — 2 ردیف (تخمین)
- کلیدها: PK_Nationality(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | tinyint(1) | خیر | بله |  |
| `NationalityName` | nvarchar(100) | خیر | — |  |
| `Description` | nvarchar(1000) | بله | — |  |

### `NewYearGiftCalculationType` — 2 ردیف (تخمین)
- کلیدها: <text 29>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `TypeName` | nvarchar(100) | خیر | — |  |
| `Description` | nvarchar(800) | بله | — |  |

### `PackagePrize` — 0 ردیف (تخمین)
- کلیدها: PK_PackagePrize_1(PK)=Rdf, PrizeNumber
- FK `FK_PackagePrize_CITYS`: CityID → CITYS.RDF
- FK `FK_PackagePrize_custgroup`: CusGroup → custgroup.group_rdf
- FK `FK_PackagePrize_inventory`: ShkaPrize → inventory.shka
- FK `FK_PackagePrize_masir`: PathID → masir.rdf_masir
- FK `FK_PackagePrize_osystems`: SysID → osystems.rdf_system
- FK `FK_PackagePrize_PackagePrize`: Rdf → PackagePrize.Rdf
- FK `FK_PackagePrize_PackagePrize`: PrizeNumber → PackagePrize.PrizeNumber
- FK `FK_PackagePrize_Province`: ProvinceID → Province.ProvinceID
- FK `FK_PackagePrize_Quarter`: QuarterID → Quarter.ID
- FK `FK_PackagePrize_regions`: RegionID → regions.rdf_region
- FK `FK_PackagePrize_sys_users`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Rdf` | bigint(8) | خیر | — |  |
| `PrizeNumber` | int(4) | خیر | — |  |
| `DoneDate` | nvarchar(100) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |
| `Package‍Items` | int(4) | خیر | — |  |
| `ShkaPrize` | bigint(8) | بله | — |  |
| `TedadPrize` | decimal(9) | بله | — |  |
| `PercentPrize` | decimal(9) | بله | — |  |
| `CusGroup` | int(4) | بله | — | ((0)) |
| `SysID` | int(4) | بله | — | ((0)) |
| `ProvinceID` | int(4) | بله | — | ((0)) |
| `CityID` | int(4) | بله | — | ((0)) |
| `RegionID` | int(4) | بله | — | ((0)) |
| `PathID` | int(4) | بله | — | ((0)) |
| `FromDate` | nvarchar(20) | بله | — |  |
| `ToDate` | nvarchar(20) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `PackageName` | nvarchar(1000) | خیر | — |  |
| `QuarterID` | int(4) | بله | — |  |

### `PackagePrizeDetails` — 0 ردیف (تخمین)
- کلیدها: PK_PackagePrizeDetails(PK)=ID, Rdf
- FK `FK_PackageDetails_inventory`: Shka → inventory.shka
- FK `<text 35>`: Rdf → PackagePrize.Rdf
- FK `<text 35>`: PrizeNumber → PackagePrize.PrizeNumber

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Rdf` | bigint(8) | خیر | — |  |
| `PrizeNumber` | int(4) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `Tedad` | decimal(9) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |

### `PardakhtKerayeHamlDetails` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=RowID
- FK `<text 38>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Shfackh` | bigint(8) | خیر | — |  |
| `Active` | nvarchar(2) | خیر | — |  |
| `RdfEdit` | int(4) | خیر | — |  |
| `ActID` | int(4) | خیر | — |  |
| `Price` | decimal(9) | خیر | — |  |
| `RdfPutCheck` | bigint(8) | بله | — |  |
| `RdfBank` | int(4) | بله | — |  |
| `RowID` | int(4) | خیر | بله |  |
| `Explain` | nvarchar(1000) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `Karmozd` | decimal(9) | بله | — |  |
| `RdfZirSarfasl` | int(4) | بله | — |  |
| `ZirSanadId` | int(4) | بله | — |  |

### `PardakhtMultiFactor` — 0 ردیف (تخمین)
- کلیدها: PK_PardakhtMultiFactor(PK)=GhnoPardakht, Rdf_, ShBuyFact

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GhnoPardakht` | int(4) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `ShBuyFact` | bigint(8) | خیر | — |  |
| `Mablagh` | money(8) | خیر | — |  |
| `IsTasviyeh` | bit(1) | خیر | — |  |

### `PassCheckAutomatic` — 0 ردیف (تخمین)
- کلیدها: PK_PassCheckAutomatic_1(PK)=RdfPutCheck

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Date` | nvarchar(20) | خیر | — |  |
| `RdfPutCheck` | bigint(8) | خیر | — |  |
| `BankRdf` | int(4) | خیر | — |  |
| `putchkmab` | decimal(9) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `UserID` | int(4) | بله | — |  |

### `PayrollPayment` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `FK_PayrollPayment_Banks`: BankID → Banks.ID
- FK `FK_PayrollPayment_Personnel`: PersonnelID → Personnel.ID
- FK `FK_PayrollPayment_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PayablePrice` | money(8) | خیر | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `CashPayment` | bit(1) | خیر | — |  |
| `CashPaymentPrice` | money(8) | بله | — |  |
| `BankTransfer` | bit(1) | خیر | — |  |
| `BankTransferPrice` | money(8) | بله | — |  |
| `ChequePayment` | bit(1) | خیر | — |  |
| `ChequePaymentPrice` | money(8) | بله | — |  |
| `AccountNumber` | nvarchar(600) | خیر | — |  |
| `BankID` | int(4) | خیر | — |  |
| `Active` | bit(1) | بله | — |  |

### `Payslip` — 0 ردیف (تخمین)
- کلیدها: PK_SalaryReceipt(PK)=ID
- FK `FK_Payslip_sys_users`: InsertBy → sys_users.user_id
- FK `FK_Payslip_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_SalaryReceipt_Months`: Month → Months.ID
- FK `FK_SalaryReceipt_Personnel`: PersonnelID → Personnel.ID
- FK `FK_SalaryReceipt_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `Month` | smallint(2) | خیر | — |  |
| `Year` | int(4) | خیر | — |  |
| `EndOfWorkAllowance` | money(8) | خیر | — |  |
| `CreditOfLeave` | money(8) | خیر | — |  |
| `Transportation` | money(8) | خیر | — |  |
| `OtherBenefits` | money(8) | خیر | — |  |
| `SupervisionAllowance` | money(8) | خیر | — |  |
| `ChildAllowance` | money(8) | بله | — |  |
| `HousingAllowance` | money(8) | بله | — |  |
| `MealStipend` | money(8) | بله | — |  |
| `HireAllowance` | money(8) | بله | — |  |
| `BadWeatherAllowance` | money(8) | بله | — |  |
| `HasOvertime` | bit(1) | خیر | — |  |
| `OvertimeTime` | nchar(20) | بله | — |  |
| `OvertimePrice` | money(8) | بله | — |  |
| `HasShiftWork` | bit(1) | خیر | — |  |
| `ShiftWorkTime` | nchar(20) | بله | — |  |
| `ShiftWorkPrice` | money(8) | بله | — |  |
| `HasNightWork` | bit(1) | خیر | — |  |
| `NightWorkTime` | nchar(20) | بله | — |  |
| `NightWorkPrice` | money(8) | بله | — |  |
| `HasMission` | bit(1) | خیر | — |  |
| `MissionTime` | nchar(20) | بله | — |  |
| `MissionPrice` | money(8) | بله | — |  |
| `NewYearGiftPrice` | money(8) | خیر | — |  |
| `RewardPrice` | money(8) | بله | — |  |
| `HasWorkInVacation` | bit(1) | خیر | — |  |
| `WorkInVacationTime` | nchar(20) | بله | — |  |
| `WorkInVacationPrice` | money(8) | بله | — |  |
| `EndOfWorkBenefits` | money(8) | خیر | — |  |
| `AdvanceMoney` | money(8) | بله | — |  |
| `Loan` | money(8) | بله | — |  |
| `OtherDeductions` | money(8) | بله | — |  |
| `AdditionInsurance` | money(8) | بله | — |  |
| `SalaryBase` | money(8) | خیر | — |  |
| `TotalSalaries` | money(8) | خیر | — |  |
| `TaxOfSalaries` | money(8) | خیر | — |  |
| `TotalInsuredSalaries` | money(8) | خیر | — |  |
| `TotalNotInsuredSalaries` | money(8) | خیر | — |  |
| `TotalInsuredAndNotInsured` | money(8) | خیر | — |  |
| `WorkerInsurance` | money(8) | خیر | — |  |
| `EmployerInsurance` | money(8) | خیر | — |  |
| `UnemploymentInsurance` | money(8) | بله | — |  |
| `TotalDeductions` | money(8) | بله | — |  |
| `NetSalaryReceived` | money(8) | خیر | — |  |
| `NewYearGiftCalculationID` | int(4) | بله | — |  |
| `DailyWorkTime` | bit(1) | خیر | — |  |
| `DailyWorkTimeValue` | float(8) | بله | — |  |
| `HourlyWorkTime` | bit(1) | خیر | — |  |
| `HourlyWorkTimeValue` | nchar(20) | بله | — |  |
| `Description` | nvarchar(1000) | بله | — |  |
| `ConstantDailySalary` | money(8) | بله | — |  |
| `ConstantHourlySalary` | money(8) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `PayrollDeduction` | money(8) | بله | — |  |
| `FractionWorkPrice` | money(8) | بله | — |  |
| `FractionWorkTime` | bigint(8) | بله | — |  |
| `AdditionOrDeduction` | money(8) | بله | — |  |
| `AtiranDocNumber` | int(4) | بله | — |  |
| `MarriagePrivilege` | money(8) | خیر | — | ((0)) |
| `MissionDays` | int(4) | خیر | — | ((0)) |
| `ShiftWork1Time` | float(8) | خیر | — | ((0)) |
| `ShiftWork2Time` | float(8) | خیر | — | ((0)) |
| `ShiftWork3Time` | float(8) | خیر | — | ((0)) |
| `IncludeInsurance` | bit(1) | بله | — |  |
| `WorkerInsurancePercent` | float(8) | بله | — |  |
| `EmployerInsurancePercent` | float(8) | بله | — |  |
| `UnemploymentInsurancePercent` | float(8) | بله | — |  |
| `MissionDailyPrice` | money(8) | بله | — |  |
| `OvertimePerMinutePrice` | money(8) | بله | — |  |
| `NightWorkPerMinutePrice` | money(8) | بله | — |  |
| `MissionPerMinutePrice` | money(8) | بله | — |  |
| `WorkInVacationPerMinutePrice` | money(8) | بله | — |  |
| `WorkDeductionPerMinutePrice` | money(8) | بله | — |  |
| `WorkInFridayCount` | int(4) | بله | — |  |
| `WorkInFridayPrice` | money(8) | بله | — |  |

### `Permission` — 5 ردیف (تخمین)
- کلیدها: PK_Permission(PK)=PermissionId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `PermissionId` | int(4) | خیر | بله |  |
| `PermissionName` | nvarchar(2000) | خیر | — |  |

### `PermissionTemp` — 0 ردیف (تخمین)
- کلیدها: PK_PermissionTemp_1(PK)=FormID, FieldID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Type` | int(4) | خیر | — |  |
| `FormID` | int(4) | خیر | — |  |
| `FieldID` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `Personnel` — 0 ردیف (تخمین)
- کلیدها: PK_Personnel(PK)=ID
- FK `FK_Personnel_Cities`: IssuancePlaceID → Cities.ID
- FK `FK_Personnel_Country`: CountryID → Country.ID
- FK `<text 30>`: EducationalDegreeID → EducationalDegree.ID
- FK `FK_Personnel_EmployeeStatus`: PersonnelStatusID → EmployeeStatus.ID
- FK `FK_Personnel_Gender`: GenderID → Gender.ID
- FK `FK_Personnel_Insurance`: InsuranceTypeID → Insurance.ID
- FK `FK_Personnel_MaritalStatus`: MaritalStatusId → MaritalStatus.Id
- FK `<text 37>`: MilitaryServiceSituationId → MilitaryServiceSituation.Id
- FK `FK_Personnel_Nationality`: NationalityID → Nationality.ID
- FK `<text 39>`: NewYearGiftCalculationTypeID → NewYearGiftCalculationType.ID
- FK `FK_Personnel_Personnel`: JobID → Jobs.ID
- FK `FK_Personnel_PersonnelGroup`: PersonnelGroupID → PersonnelGroup.ID
- FK `FK_Personnel_ShiftType`: ShiftTypeID → ShiftType.ID
- FK `FK_Personnel_sys_users`: InsertBy → sys_users.user_id
- FK `FK_Personnel_sys_users1`: UpdateBy → sys_users.user_id
- FK `<text 29>`: TaxExemptionTypeID → TaxExemptionType.ID
- FK `FK_Personnel_Workhouse`: WorkhouseID → Workhouse.ID
- FK `FK_Personnel_WorkingShifts`: DefaultShiftID → WorkingShifts.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `PersonnelNumber` | bigint(8) | خیر | — |  |
| `ShamsiBirthDate` | nchar(100) | خیر | — |  |
| `BirthDate` | datetime(8) | خیر | — |  |
| `ShamsiHireDate` | nchar(100) | خیر | — |  |
| `HireDate` | datetime(8) | خیر | — |  |
| `InsuranceTypeID` | int(4) | خیر | — |  |
| `IssuancePlaceID` | int(4) | خیر | — |  |
| `PersonnelFirstName` | nvarchar(300) | خیر | — |  |
| `PersonnelLastName` | nvarchar(300) | خیر | — |  |
| `NationalCode` | nvarchar(200) | خیر | — |  |
| `PersonnelStatusID` | int(4) | خیر | — |  |
| `GenderID` | int(4) | خیر | — |  |
| `BirthCertificate` | nvarchar(100) | خیر | — |  |
| `FatherName` | nvarchar(200) | خیر | — |  |
| `PostalCode` | nvarchar(200) | خیر | — |  |
| `PhoneNumber` | nvarchar(100) | خیر | — |  |
| `MobileNumber` | nvarchar(100) | خیر | — |  |
| `EducationalDegreeID` | int(4) | خیر | — |  |
| `StudyFieldID` | int(4) | خیر | — |  |
| `StudyFieldName` | nvarchar(400) | بله | — |  |
| `JobID` | int(4) | بله | — |  |
| `JobTitle` | nvarchar(200) | خیر | — |  |
| `Address` | nvarchar(600) | خیر | — |  |
| `JobGroupName` | nvarchar(300) | خیر | — |  |
| `MonthlyWorkingHours` | float(8) | خیر | — |  |
| `BaseMonthlySalary` | money(8) | خیر | — |  |
| `ConstantDailySalary` | money(8) | خیر | — |  |
| `ConstantHourlySalary` | money(8) | خیر | — |  |
| `HireAllowance` | money(8) | خیر | — |  |
| `MissionAllowancePercent` | float(8) | خیر | — |  |
| `CooperationTypeID` | int(4) | خیر | — |  |
| `TaxPercent` | float(8) | خیر | — |  |
| `EndOfWorkBenefitsBase` | money(8) | خیر | — |  |
| `SupervisionAllowance` | money(8) | خیر | — |  |
| `AdditionInsurance` | money(8) | خیر | — |  |
| `MonthlyAgreementTime` | smallint(2) | خیر | — |  |
| `IsIncludedInsurance` | bit(1) | خیر | — |  |
| `WorkerInsurancePercent` | float(8) | خیر | — |  |
| `EmployerInsurancePercent` | float(8) | خیر | — |  |
| `UnemploymentInsurancePercent` | float(8) | خیر | — |  |
| `TaxExemptionTypeID` | int(4) | خیر | — |  |
| `NewYearGiftCalculationTypeID` | int(4) | خیر | — |  |
| `NationalityID` | tinyint(1) | بله | — |  |
| `CountryID` | int(4) | بله | — |  |
| `BirthCertificateSerialNum1` | int(4) | بله | — |  |
| `BirthCertificateSerialNum2` | int(4) | بله | — |  |
| `BirthCertificateSerialWord` | nvarchar(100) | بله | — |  |
| `InsuranceNumber` | nvarchar(400) | بله | — |  |
| `HasChildAllowance` | bit(1) | بله | — |  |
| `NumberOfChilds` | int(4) | بله | — |  |
| `ChildAllowancePrice` | money(8) | بله | — |  |
| `HasHousingAllowance` | bit(1) | بله | — |  |
| `HousingAllowancePrice` | money(8) | بله | — |  |
| `HasMealStipend` | bit(1) | بله | — |  |
| `MealStipendPrice` | money(8) | بله | — |  |
| `HasBadWeatherAllowance` | bit(1) | بله | — |  |
| `BadWeatherAllowancePrice` | money(8) | بله | — |  |
| `OtherBenefits` | money(8) | بله | — |  |
| `BankName` | nvarchar(600) | بله | — |  |
| `BranchName` | nvarchar(600) | بله | — |  |
| `BankAccountNumber` | nvarchar(600) | بله | — |  |
| `CardNumber` | nvarchar(200) | بله | — |  |
| `ShabaNumber` | nvarchar(600) | بله | — |  |
| `Description` | nvarchar(1000) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `LeaveWork` | bit(1) | بله | — |  |
| `OvertimePrice` | money(8) | بله | — |  |
| `NightWorkPrice` | money(8) | بله | — |  |
| `MissionPrice` | money(8) | بله | — |  |
| `WorkInVacationPrice` | money(8) | بله | — |  |
| `DailyMissionPrice` | money(8) | بله | — |  |
| `LeaveWorkDate` | datetime(8) | بله | — |  |
| `PersonnelGroupID` | int(4) | بله | — |  |
| `ShiftTypeID` | int(4) | بله | — |  |
| `DefaultShiftID` | int(4) | بله | — |  |
| `EndOfWorkBenefits` | money(8) | خیر | — | ((0)) |
| `MarriagePrivilege` | money(8) | خیر | — | ((0)) |
| `WorkDeductionPrice` | money(8) | خیر | — | ((0)) |
| `MaritalStatusId` | int(4) | بله | — |  |
| `MilitaryServiceSituationId` | int(4) | بله | — |  |
| `Workplace` | nvarchar(200) | بله | — |  |

### `PersonnelGroup` — 0 ردیف (تخمین)
- کلیدها: PK_PersonnelGroup(PK)=ID
- FK `FK_PersonnelGroup_sys_users`: InsertBy → sys_users.user_id
- FK `FK_PersonnelGroup_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_PersonnelGroup_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `GroupName` | nvarchar(100) | خیر | — |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `Description` | nvarchar(600) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `PersonnelGroupAccounts` — 0 ردیف (تخمین)
- کلیدها: PK_PersonnelGroupAccounts(PK)=Id
- FK `<text 34>`: AccountId → Accounts.Id
- FK `<text 40>`: PersonnelGroupId → PersonnelGroup.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `PersonnelGroupId` | int(4) | خیر | — |  |
| `AccountId` | int(4) | خیر | — |  |
| `KolId` | bigint(8) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — |  |
| `TafsilId` | bigint(8) | بله | — |  |

### `PersonnelGroupInformation` — 0 ردیف (تخمین)
- کلیدها: PK_PersonnelGroupInformation(PK)=ID
- FK `<text 44>`: CooperationTypeID → CooperationTypes.ID
- FK `<text 55>`: NewYearGiftCalculationTypeID → NewYearGiftCalculationType.ID
- FK `<text 43>`: PersonnelGroupID → PersonnelGroup.ID
- FK `<text 38>`: ShiftTypeID → ShiftType.ID
- FK `<text 45>`: TaxExemptionTypeID → TaxExemptionType.ID
- FK `<text 42>`: DefaultShiftID → WorkingShifts.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PersonnelGroupID` | int(4) | خیر | — |  |
| `MonthlyWorkingHours` | int(4) | بله | — |  |
| `BaseMonthlySalary` | money(8) | بله | — |  |
| `ConstantDailySalary` | money(8) | بله | — |  |
| `ConstantHourlySalary` | money(8) | بله | — |  |
| `HireAllowance` | money(8) | بله | — |  |
| `MissionAllowancePercent` | float(8) | بله | — |  |
| `CooperationTypeID` | int(4) | بله | — |  |
| `TaxPercent` | float(8) | بله | — |  |
| `EndOfWorkBenefitsBase` | money(8) | بله | — |  |
| `SupervisionAllowance` | money(8) | بله | — |  |
| `AdditionInsurance` | money(8) | بله | — |  |
| `MonthlyAgreementTime` | smallint(2) | بله | — |  |
| `IsIncludedInsurance` | bit(1) | بله | — |  |
| `WorkerInsurancePercent` | float(8) | بله | — |  |
| `EmployerInsurancePercent` | float(8) | بله | — |  |
| `UnemploymentInsurancePercent` | float(8) | بله | — |  |
| `TaxExemptionTypeID` | int(4) | بله | — |  |
| `NewYearGiftCalculationTypeID` | int(4) | بله | — |  |
| `HasChildAllowance` | bit(1) | بله | — |  |
| `ChildAllowancePrice` | money(8) | بله | — |  |
| `HasHousingAllowance` | bit(1) | بله | — |  |
| `HousingAllowancePrice` | money(8) | بله | — |  |
| `HasMealStipend` | bit(1) | بله | — |  |
| `MealStipendPrice` | money(8) | بله | — |  |
| `HasBadWeatherAllowance` | bit(1) | بله | — |  |
| `BadWeatherAllowancePrice` | money(8) | بله | — |  |
| `OvertimePrice` | money(8) | بله | — |  |
| `NightWorkPrice` | money(8) | بله | — |  |
| `MissionPrice` | money(8) | بله | — |  |
| `WorkInVacationPrice` | money(8) | بله | — |  |
| `DailyMissionPrice` | money(8) | بله | — |  |
| `ShiftTypeID` | int(4) | بله | — |  |
| `DefaultShiftID` | int(4) | بله | — |  |
| `EndOfWorkBenefits` | money(8) | بله | — |  |
| `MarriagePrivilege` | money(8) | بله | — |  |
| `WorkDeductionPrice` | money(8) | خیر | — | ((0)) |

### `PersonnelImages` — 0 ردیف (تخمین)
- کلیدها: PK_PersonnelImages(PK)=ID
- FK `FK_PersonnelImages_Personnel`: PersonnelID → Personnel.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `PersonnelImage` | image(16) | بله | — |  |
| `NationalCardImage1` | image(16) | بله | — |  |
| `NationalCardImage2` | image(16) | بله | — |  |
| `BirthCertificateImage1` | image(16) | بله | — |  |
| `BirthCertificateImage2` | image(16) | بله | — |  |
| `BirthCertificateImage3` | image(16) | بله | — |  |

### `PersonnelShiftWork` — 0 ردیف (تخمین)
- کلیدها: PK_PersonnelShiftWork(PK)=ID
- FK `<text 31>`: PersonnelID → Personnel.ID
- FK `<text 31>`: ShiftWorkID → ShiftWork.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ShiftWorkID` | int(4) | خیر | — |  |
| `WorkTime` | nchar(20) | خیر | — |  |
| `PersonnelID` | int(4) | خیر | — |  |

### `PersonnelsAccounts` — 0 ردیف (تخمین)
- کلیدها: PK_PersonnelsAccounts(PK)=Id
- FK `<text 30>`: AccountId → Accounts.Id
- FK `<text 31>`: PersonnelId → Personnel.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `PersonnelId` | int(4) | خیر | — |  |
| `AccountId` | int(4) | خیر | — |  |
| `KolId` | bigint(8) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — |  |
| `TafsilId` | bigint(8) | بله | — |  |

### `PeygiriMotalebat` — 0 ردیف (تخمین)
- کلیدها: PK_PeygiriMotalebat(PK)=ID
- FK `<text 36>`: Shmo → CUSTOMERS.SHMO

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Done_Date` | nchar(20) | خیر | — |  |
| `FollowUpDate` | nchar(20) | خیر | — |  |
| `Shmo` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `CashAmount` | money(8) | خیر | — | ((0)) |
| `PosAmount` | money(8) | خیر | — | ((0)) |
| `PosExplain` | nvarchar(1000) | بله | — |  |
| `CheckAmount` | money(8) | خیر | — | ((0)) |
| `CheckExplain` | nvarchar(-1) | بله | — |  |
| `Explain` | nvarchar(-1) | بله | — |  |
| `Condition` | bit(1) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — |  |
| `FollowUpStatus` | bit(1) | خیر | — | ((0)) |
| `Rdf_Edit` | int(4) | خیر | — | ((1)) |
| `Active` | bit(1) | خیر | — | ((1)) |

### `PeygiriMotalebatHistory` — 0 ردیف (تخمین)
- کلیدها: PK_PeygiriMotalebatHistory(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Done_Date` | nchar(20) | خیر | — |  |
| `FollowUpDate` | nchar(20) | خیر | — |  |
| `Shmo` | bigint(8) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `CashAmount` | money(8) | خیر | — |  |
| `PosAmount` | money(8) | خیر | — |  |
| `PosExplain` | nvarchar(1000) | بله | — |  |
| `CheckAmount` | money(8) | خیر | — |  |
| `CheckExplain` | nvarchar(-1) | بله | — |  |
| `Explain` | nvarchar(-1) | بله | — |  |
| `Condition` | bit(1) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — |  |
| `FollowUpStatus` | bit(1) | خیر | — | ((0)) |
| `ShomareSanad` | int(4) | خیر | — | ((1)) |
| `Rdf_Edit` | int(4) | خیر | — |  |

### `Phase` — 0 ردیف (تخمین)
- کلیدها: PK_Phases(PK)=PhaseID
- FK `FK_Phase_project`: ProjectID → project.ProjectID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `PhaseID` | int(4) | خیر | بله |  |
| `PhaseName` | nvarchar(200) | بله | — |  |
| `parentPhaseID` | int(4) | بله | — |  |
| `ProjectID` | int(4) | بله | — |  |
| `startDate` | nvarchar(20) | بله | — |  |
| `endDate` | nvarchar(20) | بله | — |  |

### `PishDaryaft` — 0 ردیف (تخمین)
- کلیدها: PK_PishDaryaft(PK)=GhnoPishDaryaft
- FK `FK_PishDaryaft_CUSTOMERS`: Shmo → CUSTOMERS.SHMO
- FK `FK_PishDaryaft_osystems`: SysID → osystems.rdf_system
- FK `FK_PishDaryaft_sys_users`: UserTaeedHesabdari → sys_users.user_id
- FK `FK_PishDaryaft_sys_users1`: UserRejectedHesabdari → sys_users.user_id
- FK `FK_PishDaryaft_visitors`: VisitorID → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GhnoPishDaryaft` | int(4) | خیر | بله |  |
| `Naghd` | decimal(9) | خیر | — |  |
| `Date` | nvarchar(20) | خیر | — |  |
| `Comment` | nvarchar(1000) | خیر | — |  |
| `Shmo` | int(4) | خیر | — |  |
| `DateRecive` | nvarchar(20) | خیر | — |  |
| `Shfac` | int(4) | خیر | — | ((0)) |
| `VisitorID` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `ClockTime` | nvarchar(20) | خیر | — |  |
| `UserTaeedHesabdari` | int(4) | بله | — |  |
| `DateTaeedHesabdari` | nvarchar(20) | بله | — |  |
| `TaeedHesabdari` | bit(1) | خیر | — | ((0)) |
| `UserRejectedHesabdari` | int(4) | بله | — |  |
| `DateRejectedHesabdari` | nvarchar(20) | بله | — |  |
| `Rejected` | bit(1) | خیر | — | ((0)) |
| `RejectedComment` | nvarchar(1000) | خیر | — |  |
| `GhnoInDar` | int(4) | خیر | — | ((0)) |
| `ExtraPrice` | decimal(9) | خیر | — |  |
| `Tafif` | decimal(9) | بله | — |  |

### `PishDaryaftGetCheck` — 0 ردیف (تخمین)
- کلیدها: PK_PishDaryaftGetCheck(PK)=RowID
- FK `<text 34>`: GhnoPishDaryaft → PishDaryaft.GhnoPishDaryaft

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `GhnoPishDaryaft` | int(4) | خیر | — |  |
| `sardate` | nvarchar(20) | خیر | — |  |
| `ShomareHesab` | nvarchar(1000) | خیر | — |  |
| `BankName` | nvarchar(1000) | خیر | — |  |
| `Shobe` | nvarchar(1000) | خیر | — |  |
| `Serial` | nvarchar(1000) | خیر | — |  |
| `Price` | decimal(9) | خیر | — |  |
| `Desc` | nvarchar(4000) | بله | — |  |
| `ShenaseSayad` | nvarchar(200) | بله | — |  |

### `PishDaryaftMultiFactor` — 0 ردیف (تخمین)
- کلیدها: PK_PishDaryaftMultiFactor(PK)=RowID
- FK `<text 37>`: GhnoPishDaryaft → PishDaryaft.GhnoPishDaryaft

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `GhnoPishDaryaft` | int(4) | خیر | — |  |
| `Shfacfo` | int(4) | خیر | — |  |
| `Price` | decimal(9) | خیر | — |  |

### `PishDaryaftPos` — 0 ردیف (تخمین)
- کلیدها: PK_PishDaryaftPos(PK)=RowID
- FK `FK_PishDaryaftPos_BANK`: PosBankRdf → BANK.RDF
- FK `<text 29>`: GhnoPishDaryaft → PishDaryaft.GhnoPishDaryaft

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `GhnoPishDaryaft` | int(4) | خیر | — |  |
| `price` | decimal(9) | خیر | — |  |
| `PosBankRdf` | int(4) | خیر | — |  |
| `ShomarePeygiri` | nvarchar(1000) | خیر | — |  |
| `IsPDA` | bit(1) | خیر | — | ((0)) |

### `PopupSettingKind` — 6 ردیف (تخمین)
- کلیدها: PK_PopupSettingKind(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |

### `PopupSettings` — 0 ردیف (تخمین)
- کلیدها: PK_PopupSettings(PK)=RowID
- FK `<text 33>`: SettingID → PopupSettingKind.ID
- FK `FK_PopupSettings_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `SettingID` | int(4) | خیر | — |  |
| `SettingValue` | int(4) | خیر | — | ((0)) |
| `SettingOption` | int(4) | خیر | — | ((0)) |
| `UserID` | int(4) | خیر | — |  |
| `ShowDate` | varchar(10) | بله | — |  |

### `PosDetails` — 1083 ردیف (تخمین)
- کلیدها: PK_PosDetails(PK)=ID, ghno, PosBankRdf, Rdf_
- FK `FK_PosDetails_BANK`: PosBankRdf → BANK.RDF
- FK `FK_PosDetails_dar`: ghno → dar.ghno
- FK `FK_PosDetails_dar`: p → dar.p
- FK `FK_PosDetails_dar`: Rdf_ → dar.Rdf_

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ghno` | int(4) | خیر | — |  |
| `p` | int(4) | خیر | — |  |
| `MabPos` | money(8) | خیر | — |  |
| `PosBankRdf` | int(4) | خیر | — |  |
| `PosDesc` | nvarchar(3000) | بله | — |  |
| `ShPeigiri` | nvarchar(1000) | بله | — |  |
| `IsHavaleh` | bit(1) | بله | — | ((0)) |
| `Rdf_` | int(4) | خیر | — | ((1)) |
| `Karmozd` | decimal(9) | بله | — |  |
| `ZirSanad` | int(4) | بله | — |  |
| `RdfZirSarFasl` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `TerminalID` | int(4) | بله | — |  |
| `isEdited` | bit(1) | خیر | — | ((0)) |

### `PosDetailsInAccounting` — 0 ردیف (تخمین)
- کلیدها: PK_PosDetailsInAccounting(PK)=RowID
- FK `<text 30>`: PosBankRdf → BANK.RDF
- FK `<text 43>`: Ghno → DaryaftVaPardakht.Ghno
- FK `<text 43>`: Rdf_ → DaryaftVaPardakht.Rdf_
- FK `<text 43>`: IsDaryaft → DaryaftVaPardakht.IsDaryaft

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `Ghno` | int(4) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `Price` | decimal(9) | خیر | — |  |
| `PosBankRdf` | int(4) | خیر | — |  |
| `IsPos` | bit(1) | خیر | — |  |
| `ShPeygiri` | nvarchar(100) | خیر | — |  |
| `Desc` | nvarchar(1000) | خیر | — |  |
| `IsDaryaft` | bit(1) | خیر | — |  |
| `Karmozd` | decimal(9) | خیر | — | ((0)) |

### `PrizeTasaodi` — 0 ردیف (تخمین)
- کلیدها: PK_PrizeTasaodi(PK)=rdf
- FK `FK_PrizeTasaodi_CITYS`: CityID → CITYS.RDF
- FK `FK_PrizeTasaodi_custgroup`: CusGroup → custgroup.group_rdf
- FK `FK_PrizeTasaodi_inventory`: ShkaPrize → inventory.shka
- FK `FK_PrizeTasaodi_masir`: PathID → masir.rdf_masir
- FK `FK_PrizeTasaodi_osystems`: SysID → osystems.rdf_system
- FK `FK_PrizeTasaodi_PrizeTasaodi`: Shka → inventory.shka
- FK `FK_PrizeTasaodi_Province`: ProvinceID → Province.ProvinceID
- FK `FK_PrizeTasaodi_Quarter`: QuarterID → Quarter.ID
- FK `FK_PrizeTasaodi_regions`: RegionID → regions.rdf_region
- FK `FK_PrizeTasaodi_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `Shka` | bigint(8) | خیر | — | ((0)) |
| `Tedad` | int(4) | خیر | — |  |
| `ShkaPrize` | bigint(8) | خیر | — |  |
| `TedadPrize` | float(8) | خیر | — |  |
| `PrizeFromOrginalProduct` | bit(1) | خیر | — | ((0)) |
| `CusGroup` | int(4) | بله | — | ((0)) |
| `SysID` | int(4) | بله | — | ((0)) |
| `ProvinceID` | int(4) | بله | — | ((0)) |
| `CityID` | int(4) | بله | — | ((0)) |
| `RegionID` | int(4) | بله | — | ((0)) |
| `PathID` | int(4) | بله | — | ((0)) |
| `FromDate` | nvarchar(20) | بله | — |  |
| `ToDate` | nvarchar(20) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |
| `QuarterID` | int(4) | بله | — |  |

### `ProductProperty` — 5 ردیف (تخمین)
- کلیدها: PK_ProductProperty(PK)=ProductPropertyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ProductPropertyID` | int(4) | خیر | بله |  |
| `ProductID` | int(4) | بله | — |  |
| `PropertyName` | varchar(50) | بله | — |  |
| `PropertyPrice` | money(8) | بله | — |  |

### `ProductScaleMemoryAssignment` — 0 ردیف (تخمین)
- کلیدها: <text 31>(PK)=ID
- FK `<text 41>`: ProductId → inventory.shka
- FK `<text 41>`: ScaleTypeId → ScaleType.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ScaleTypeId` | int(4) | خیر | — |  |
| `ScaleId` | int(4) | خیر | — |  |
| `ProductId` | bigint(8) | خیر | — |  |
| `MemoryId` | int(4) | خیر | — |  |
| `PluId` | int(4) | خیر | — |  |

### `Production` — 42 ردیف (تخمین)
- کلیدها: <text 29>(PK)=ID, Rdf_
- FK `FK_Production_anbars`: rdf_anbar → anbars.rdf_anbar
- FK `FK_Production_Formulation`: FormulID → Formulation.ID
- FK `FK_Production_inventory`: shka → inventory.shka
- FK `FK_Production_sys_users`: UserId → sys_users.user_id
- FK `FK_Production_sys_users1`: UserIdConfirm → sys_users.user_id
- FK `FK_Production_sys_usersACC`: UserIdConfirmAcc → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `FormulID` | bigint(8) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `TedadVahed` | decimal(9) | خیر | — |  |
| `TedBastebandi` | decimal(9) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |
| `DoneTime` | nvarchar(20) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `rdf_anbar` | int(4) | خیر | — |  |
| `dateProduction` | nvarchar(20) | خیر | — |  |
| `IsManual` | bit(1) | خیر | — | ((0)) |
| `IsConfirm` | bit(1) | بله | — | ((0)) |
| `DateConfirm` | char(10) | بله | — |  |
| `TimeConfirm` | char(8) | بله | — |  |
| `UserIdConfirm` | int(4) | بله | — |  |
| `IsConfirmAcc` | bit(1) | بله | — | ((0)) |
| `DateConfirmAcc` | char(10) | بله | — |  |
| `TimeConfirmAcc` | char(8) | بله | — |  |
| `UserIdConfirmAcc` | int(4) | بله | — |  |
| `AccDocNum` | int(4) | بله | — |  |

### `ProductionCost` — 39 ردیف (تخمین)
- کلیدها: PK_ProductionCost_1(PK)=ID
- FK `<text 30>`: CostID → IndirectCost.RowId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `Rdf_` | int(4) | خیر | — |  |
| `ProductionID` | bigint(8) | خیر | — |  |
| `CostID` | int(4) | خیر | — |  |
| `Price` | money(8) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `ProductionSeries` — 42 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `<text 29>`: shka → inventory.shka
- FK `<text 40>`: TypePS → ProductionSeriesType.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `Ref_Num` | bigint(8) | خیر | — |  |
| `Rdf_` | int(4) | خیر | — |  |
| `TypePS` | int(4) | خیر | — |  |
| `CodePs` | nvarchar(100) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `PriceCustomer` | money(8) | خیر | — |  |
| `ExpDate` | nvarchar(20) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `Name` | nvarchar(100) | خیر | — |  |
| `ExpDateGregorian` | nvarchar(20) | بله | — |  |
| `PriceEnd` | money(8) | بله | — |  |
| `PriceEnd_Cost` | money(8) | بله | — |  |
| `ProductionPrice` | money(8) | بله | — | ((0)) |

### `ProductionSeriesType` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `name` | nvarchar(40) | خیر | — |  |

### `ProgramExpirationDate` — 0 ردیف (تخمین)
- کلیدها: PK_ProgramExpirationDate(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `DateExpiration` | nvarchar(20) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `Activated` | bit(1) | بله | — |  |

### `Province` — 2 ردیف (تخمین)
- کلیدها: PK_Province(PK)=ProvinceID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ProvinceID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(200) | خیر | — |  |
| `Lat` | float(8) | بله | — |  |
| `Lng` | float(8) | بله | — |  |
| `TempProvinceID` | int(4) | بله | — |  |
| `CodeTTMS` | nvarchar(100) | بله | — |  |
| `Code` | nvarchar(1000) | خیر | — |  |

### `Province_Temp` — 31 ردیف (تخمین)
- کلیدها: PK_Province_Temp(PK)=ProvinceID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ProvinceID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(200) | خیر | — |  |
| `Lat` | float(8) | بله | — |  |
| `Lng` | float(8) | بله | — |  |
| `Code` | nvarchar(1000) | خیر | — |  |

### `PublicSettings` — 2 ردیف (تخمین)
- کلیدها: PK_PublicSettings(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `cuid` | nvarchar(400) | بله | — |  |
| `exd` | varbinary(1000) | بله | — |  |
| `Description` | nvarchar(800) | بله | — |  |
| `sta` | varbinary(1000) | بله | — |  |
| `AppVersion` | nvarchar(100) | بله | — |  |
| `tcix` | varbinary(1000) | بله | — |  |
| `ivx` | varbinary(1000) | بله | — |  |
| `date` | varbinary(1000) | بله | — |  |
| `CI` | varbinary(1000) | بله | — |  |
| `pnu` | varbinary(1000) | بله | — |  |
| `tke` | varbinary(1000) | بله | — |  |
| `sk1` | varbinary(1000) | بله | — |  |
| `sk2` | varbinary(1000) | بله | — |  |
| `HLock` | bit(1) | بله | — |  |
| `HLockType` | tinyint(1) | بله | — |  |
| `HLP` | varbinary(1000) | بله | — |  |
| `RK` | varbinary(1000) | بله | — |  |
| `WK` | varbinary(1000) | بله | — |  |

### `Quarter` — 3 ردیف (تخمین)
- کلیدها: PK_Quarter(PK)=ID
- FK `FK_Quarter_regions`: RegionId → regions.rdf_region

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `RegionId` | int(4) | خیر | — |  |
| `IDInRegion` | int(4) | خیر | — |  |
| `Name` | nvarchar(100) | خیر | — |  |
| `Code` | nvarchar(1000) | خیر | — |  |

### `ReasonForRevocation` — 0 ردیف (تخمین)
- کلیدها: PK_ReasonForRevocation(PK)=RevocationID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RevocationID` | int(4) | خیر | بله |  |
| `RevocationName` | nvarchar(1000) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `Price` | money(8) | خیر | — | ((0)) |
| `Score` | int(4) | خیر | — | ((0)) |
| `ScoreActive` | bit(1) | خیر | — | ((0)) |

### `RegionVertex` — 0 ردیف (تخمین)
- کلیدها: PK_RegionVertex(PK)=RegionVertexID
- FK `FK_RegionVertex_Region`: rdf_region → regions.rdf_region

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RegionVertexID` | int(4) | خیر | بله |  |
| `rdf_region` | int(4) | خیر | — |  |
| `Order` | int(4) | خیر | — |  |
| `Lat` | float(8) | خیر | — |  |
| `Lng` | float(8) | خیر | — |  |

### `ReportColumns` — 0 ردیف (تخمین)
- کلیدها: PK_ReportColumns(PK)=ID
- FK `FK_ReportColumns_Reports`: ReportID → Reports.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ReportID` | int(4) | خیر | — |  |
| `ColumnName` | nvarchar(200) | خیر | — |  |
| `Title` | nvarchar(200) | خیر | — |  |
| `IsVisible` | bit(1) | خیر | — |  |

### `ReportFilters` — 0 ردیف (تخمین)
- کلیدها: PK_NewTable(PK)=ID
- FK `FK_NewTable_Reports`: ReportID → Reports.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ReportID` | int(4) | خیر | — |  |
| `ColumnName` | nvarchar(200) | خیر | — |  |
| `ConditionType` | nchar(20) | بله | — |  |
| `FilterID` | int(4) | بله | — |  |

### `ReportUsers` — 0 ردیف (تخمین)
- کلیدها: PK_ReportUsers(PK)=ID
- FK `FK_ReportUsers_Reports`: ReportID → Reports.ID
- FK `FK_ReportUsers_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `ReportID` | int(4) | خیر | — |  |

### `Reports` — 0 ردیف (تخمین)
- کلیدها: PK_Reports(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ReportName` | nvarchar(200) | خیر | — |  |
| `ReportQuery` | text(16) | خیر | — |  |
| `ReportCondition` | text(16) | خیر | — |  |
| `OrderByColumn` | nvarchar(200) | بله | — |  |
| `OrderType` | nvarchar(200) | بله | — |  |
| `GroupBy` | nvarchar(200) | بله | — |  |
| `HavingBy` | nvarchar(200) | بله | — |  |

### `ReportsCustom` — 0 ردیف (تخمین)
- کلیدها: PK_ReportsCustom(PK)=ID
- FK `<text 31>`: DefaultID → ReportsDefault.ID
- FK `FK_ReportsCustom_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `DefaultID` | int(4) | خیر | — |  |
| `UserID` | int(4) | بله | — |  |
| `ReportFile` | varbinary(-1) | بله | — |  |

### `ReportsDefault` — 7 ردیف (تخمین)
- کلیدها: PK_ReportsDefault(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ReportName` | nvarchar(400) | خیر | — |  |
| `ReportFileName` | nchar(600) | بله | — |  |
| `ReportFile` | varbinary(-1) | بله | — |  |

### `ReportsInForms` — 258 ردیف (تخمین)
- کلیدها: PK_FormReports(PK)=rdf
- FK `FK_ReportsInForms_Form`: FormID → Form.FormId
- FK `<text 29>`: ReportID → ReportsKind.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `ReportID` | int(4) | خیر | — |  |
| `FormID` | int(4) | خیر | — |  |

### `ReportsKind` — 235 ردیف (تخمین)
- کلیدها: PK_ReportsName(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ReportName` | nvarchar(400) | خیر | — |  |
| `ReportDesciption` | nvarchar(400) | بله | — |  |
| `ReportType` | nvarchar(400) | بله | — |  |

### `RoleFieldPermission` — 1 ردیف (تخمین)
- کلیدها: PK_RoleFieldPermission(PK)=RoleFieldPermissionId
- FK `FK_RoleFieldPermission_Field`: FieldId → Field.FieldId
- FK `<text 33>`: PermissionId → Permission.PermissionId
- FK `FK_RoleFieldPermission_Roles`: RoleId → Roles.id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RoleFieldPermissionId` | int(4) | خیر | بله |  |
| `RoleId` | int(4) | خیر | — |  |
| `FieldId` | int(4) | خیر | — |  |
| `PermissionId` | int(4) | خیر | — |  |

### `RoleFormPermission` — 0 ردیف (تخمین)
- کلیدها: PK_RoleFormPermission(PK)=RoleFormPermissionId
- FK `<text 32>`: FormId → Form.FormId
- FK `<text 40>`: RoleFormPermissionId → RoleFormPermission.RoleFormPermissionId
- FK `FK_RoleFormPermission_Roles`: RoleId → Roles.id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RoleFormPermissionId` | int(4) | خیر | بله |  |
| `RoleId` | int(4) | خیر | — |  |
| `FormId` | int(4) | خیر | — |  |
| `PermissionId` | int(4) | خیر | — |  |

### `Roles` — 7 ردیف (تخمین)
- کلیدها: PK_Roles(PK)=id
- FK `FK_Roles_SubSystem`: SubSystemId → SubSystem.SubSystemId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `name` | varchar(30) | خیر | — |  |
| `SubSystemId` | int(4) | بله | — |  |

### `SaleCircular` — 0 ردیف (تخمین)
- کلیدها: PK_SaleCircular(PK)=RDF, ID
- FK `FK_SaleCircular_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | bigint(8) | خیر | — |  |
| `ID` | bigint(8) | خیر | — |  |
| `Desc` | nvarchar(200) | بله | — |  |
| `ApplyDate` | nvarchar(20) | خیر | — |  |
| `Date` | nvarchar(20) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `isApplied` | bit(1) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `SaleCircularDetails` — 0 ردیف (تخمین)
- کلیدها: PK_SaleCircularDetails(PK)=rdf_, RDF, CircularID
- FK `<text 32>`: Shka → inventory.shka
- FK `<text 35>`: RDF → SaleCircular.RDF
- FK `<text 35>`: CircularID → SaleCircular.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_` | bigint(8) | خیر | بله |  |
| `RDF` | bigint(8) | خیر | — |  |
| `CircularID` | bigint(8) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `Forosh1` | money(8) | خیر | — |  |
| `Forosh2` | money(8) | خیر | — |  |
| `Forosh3` | money(8) | خیر | — |  |
| `Forosh4` | money(8) | خیر | — |  |
| `Forosh5` | money(8) | خیر | — |  |
| `FinalPrice` | money(8) | خیر | — |  |
| `VarietyID` | int(4) | خیر | — |  |

### `SaleFactTasvieh` — 2 ردیف (تخمین)
- کلیدها: PK_SaleFactTasvieh(PK)=TasviehID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `TasviehID` | int(4) | خیر | بله |  |
| `Description` | nvarchar(200) | بله | — |  |
| `TedRooz` | int(4) | بله | — |  |

### `SaleLimit` — 0 ردیف (تخمین)
- کلیدها: PK_LimitSale(PK)=ID, Rdf
- FK `FK_SaleLimit_sys_users`: UserID → sys_users.user_id
- FK `FK_SaleLimit_visitors`: VisitorID → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Rdf` | int(4) | خیر | — |  |
| `VisitorID` | int(4) | خیر | — |  |
| `ApplyFrom` | nvarchar(20) | خیر | — |  |
| `ApplyTo` | nvarchar(20) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |

### `SaleLimitDetails` — 0 ردیف (تخمین)
- کلیدها: PK_LimitSaleDetails(PK)=rdf, LimitID, EditID
- FK `<text 29>`: shka → inventory.shka
- FK `<text 29>`: LimitID → SaleLimit.ID
- FK `<text 29>`: EditID → SaleLimit.Rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `LimitID` | int(4) | خیر | — |  |
| `EditID` | int(4) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `TedadVahed` | decimal(9) | خیر | — |  |
| `TedadJoz` | int(4) | خیر | — |  |

### `ScaleType` — 1 ردیف (تخمین)
- کلیدها: PK_ScaleType(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `Name` | nvarchar(100) | خیر | — |  |

### `SettingTransfer` — 25 ردیف (تخمین)
- کلیدها: PK_SettingTransfer(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `dis` | nvarchar(1000) | خیر | — |  |
| `value` | int(4) | خیر | — |  |
| `desc` | nvarchar(1000) | خیر | — |  |

### `SettlementType` — 2 ردیف (تخمین)
- کلیدها: PK_SettlementType(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `SettlementTypeName` | nvarchar(100) | خیر | — |  |

### `Shakhsiat` — 5 ردیف (تخمین)
- کلیدها: PK_Shakhsiat(PK)=RDF

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | — |  |
| `Name` | varchar(50) | خیر | — |  |

### `ShareProjectToDepartment` — 0 ردیف (تخمین)
- کلیدها: PK_ShareProjectToDepartment(PK)=ID
- FK `<text 38>`: DepartmentID → department.DepartmentID
- FK `<text 35>`: ProjectID → project.ProjectID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ProjectID` | int(4) | خیر | — |  |
| `DepartmentID` | int(4) | خیر | — |  |

### `ShiftType` — 2 ردیف (تخمین)
- کلیدها: PK_ShiftType(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `TypeName` | nvarchar(100) | بله | — |  |

### `ShiftWork` — 3 ردیف (تخمین)
- کلیدها: PK_ShiftWork(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Shift` | nvarchar(100) | خیر | — |  |

### `ShortcutKey` — 45 ردیف (تخمین)
- کلیدها: PK_SurtcutKey(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `Shurtcut` | nvarchar(100) | خیر | — |  |
| `MainForm` | nvarchar(400) | خیر | — |  |
| `Dis` | nvarchar(1000) | بله | — |  |
| `EnumName` | nvarchar(1000) | بله | — |  |

### `SiteSetting` — 8 ردیف (تخمین)
- کلیدها: PK_SiteSetting(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `Title` | nvarchar(1000) | خیر | — |  |
| `Value` | int(4) | خیر | — |  |
| `Desc` | nvarchar(-1) | خیر | — |  |

### `SortStatus` — 3 ردیف (تخمین)
- کلیدها: PK_SortStatus(PK)=SortID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SortID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(1000) | خیر | — |  |

### `SortTable` — 68 ردیف (تخمین)
- کلیدها: PK_SortTable(PK)=ID
- FK `FK_SortTable_SortStatus`: SortID → SortStatus.SortID
- FK `FK_SortTable_SortTable`: TableID → TableName.TableID
- FK `FK_SortTable_UserID`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `TableID` | int(4) | خیر | — |  |
| `ColumnName` | nvarchar(1000) | خیر | — |  |
| `Visible` | bit(1) | خیر | — |  |
| `DisplayIndex` | int(4) | خیر | — |  |
| `SortID` | int(4) | خیر | — |  |
| `PersianColumnName` | nvarchar(1000) | خیر | — |  |
| `UserID` | int(4) | بله | — | (NULL) |

### `StationCanceledSaleDetails` — 0 ردیف (تخمین)
- کلیدها: <text 29>(PK)=RowID
- FK `<text 39>`: Shka → inventory.shka
- FK `<text 55>`: SaleID → StationCanceledSaleHeader.SaleID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `SaleID` | int(4) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `RdfKhat` | int(4) | خیر | — |  |
| `AnbarID` | int(4) | خیر | — |  |
| `VariteID` | int(4) | خیر | — |  |
| `Tedad` | decimal(9) | خیر | — |  |
| `Price` | money(8) | خیر | — |  |
| `PerTafif` | decimal(5) | خیر | — |  |
| `PerPromotion` | decimal(5) | خیر | — |  |
| `PerTax` | decimal(5) | خیر | — |  |
| `TafifHajmi` | money(8) | خیر | — | ((0)) |
| `Gift` | bit(1) | خیر | — |  |
| `Vazn` | decimal(9) | خیر | — | ((0)) |
| `CustomerPrice` | money(8) | خیر | — | ((0)) |

### `StationCanceledSaleHeader` — 0 ردیف (تخمین)
- کلیدها: PK_StationCanceledSaleHeader(PK)=SaleID
- FK `<text 37>`: SysID → osystems.rdf_system

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SaleID` | int(4) | خیر | بله |  |
| `UniqueID` | nvarchar(64) | خیر | — |  |
| `Shmo` | int(4) | بله | — |  |
| `Date` | varchar(10) | خیر | — |  |
| `Time` | varchar(8) | خیر | — |  |
| `DoneDate` | varchar(10) | خیر | — |  |
| `DoneTime` | varchar(8) | خیر | — |  |
| `HPriceTafif` | money(8) | خیر | — | ((0)) |
| `Barbari` | money(8) | خیر | — | ((0)) |
| `UserID` | int(4) | خیر | — |  |
| `Description` | nvarchar(-1) | خیر | — | (' ') |
| `isSync` | bit(1) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — |  |
| `ActivitionCode` | nvarchar(-1) | خیر | — | ('''') |

### `StationDocumentTransferFund` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `AmountCashReceived` | decimal(9) | بله | — |  |
| `AmountPosReceived` | decimal(9) | بله | — |  |
| `AmountCashPay` | decimal(9) | بله | — |  |
| `FromUser` | int(4) | بله | — |  |
| `ToUser` | int(4) | بله | — |  |
| `<text 30>` | decimal(9) | بله | — |  |
| `<text 32>` | decimal(9) | بله | — |  |
| `IsSync` | bit(1) | خیر | — | ((0)) |
| `Date` | nvarchar(20) | بله | — |  |
| `DoneDate` | nvarchar(20) | بله | — |  |

### `StationManagment` — 0 ردیف (تخمین)
- کلیدها: PK_StationManagment(PK)=SmID
- FK `FK_StationManagment_anbars`: AnbarSale → anbars.rdf_anbar
- FK `FK_StationManagment_osystems`: SysID → osystems.rdf_system

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SmID` | int(4) | خیر | بله |  |
| `CodeRegister` | nvarchar(-1) | بله | — |  |
| `CodeDevice` | nvarchar(-1) | بله | — |  |
| `isActive` | bit(1) | خیر | — | ((0)) |
| `AnbarSale` | int(4) | بله | — |  |
| `SysID` | int(4) | بله | — |  |
| `ReturnSaleAccess` | bit(1) | خیر | — | ((0)) |
| `EditSaleAccess` | bit(1) | خیر | — | ((0)) |

### `StationTerminals` — 0 ردیف (تخمین)
- کلیدها: PK_StationTerminals(PK)=RowID
- FK `<text 36>`: SmID → StationManagment.SmID
- FK `<text 31>`: TerminalID → TerminalPos.TerminalID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `SmID` | int(4) | بله | — |  |
| `TerminalID` | int(4) | بله | — |  |

### `SubContradiction` — 0 ردیف (تخمین)
- کلیدها: PK_SubContradiction(PK)=RowID
- FK `<text 33>`: ContradictionID → Contradiction.ContradictionID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `ContradictionID` | int(4) | خیر | — |  |
| `MainKey` | bigint(8) | خیر | — |  |
| `Explain` | nvarchar(1000) | بله | — |  |
| `ColorCode` | nvarchar(100) | بله | — | (N'#ff0000') |

### `SubMergeTbl` — 0 ردیف (تخمین)
- کلیدها: PK_SubMergeTbl(PK)=SubMergeID
- FK `FK_SubMergeTbl_Document`: DocID → Document.DocID
- FK `<text 30>`: MergeID → MergeAccountTbl.MergeID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SubMergeID` | int(4) | خیر | بله |  |
| `MergeID` | int(4) | خیر | — |  |
| `DocID` | bigint(8) | خیر | — |  |

### `SubSailSefaresh` — 0 ردیف (تخمین)
- کلیدها: PK_SubSailSefaresh(PK)=SubSailSefareshRowID
- FK `<text 30>`: RowIDSefaresh → tblSefaresh.RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SubSailSefareshRowID` | int(4) | خیر | بله |  |
| `RowIDSefaresh` | int(4) | خیر | — |  |
| `ExplainValueSefaresh` | nvarchar(4000) | خیر | — |  |
| `subSailRdf_` | int(4) | خیر | — |  |
| `SubSailShfac` | bigint(8) | خیر | — |  |
| `SubSailShka` | bigint(8) | خیر | — |  |
| `SubSailRDFKhat` | int(4) | خیر | — |  |

### `SubSystem` — 17 ردیف (تخمین)
- کلیدها: PK_SubSystem(PK)=SubSystemId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SubSystemId` | int(4) | خیر | بله |  |
| `Name` | nvarchar(2000) | خیر | — |  |
| `Status` | int(4) | بله | — |  |
| `ShowOrder` | smallint(2) | بله | — |  |

### `SubSystemPermission` — 89 ردیف (تخمین)
- کلیدها: PK_SubSystemPermission(PK)=ID
- FK `Fk_SubSystemID`: SubSystemID → SubSystem.SubSystemId
- FK `FK_UserID`: user_id → sys_users.user_id
- FK `Fk-PermissionID`: PermissionID → Permission.PermissionId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `PermissionID` | int(4) | بله | — |  |
| `SubSystemID` | int(4) | بله | — |  |
| `user_id` | int(4) | بله | — |  |

### `Sys_Bank` — 8 ردیف (تخمین)
- کلیدها: PK_Sys_Bank(PK)=Rdf
- FK `FK_Sys_Bank_BANK`: BankID → BANK.RDF
- FK `FK_Sys_Bank_osystems`: SysID → osystems.rdf_system

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `BankID` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `Rdf` | int(4) | خیر | بله |  |

### `Sys_Mandeh_Customer` — 2724 ردیف (تخمین)
- کلیدها: PK_Sys_Mandeh_Customer(PK)=ID
- FK `<text 32>`: Shmo → CUSTOMERS.SHMO
- FK `<text 31>`: SysId → osystems.rdf_system

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `Shmo` | int(4) | خیر | — |  |
| `Mandeh` | decimal(9) | خیر | — |  |
| `SysId` | int(4) | خیر | — |  |
| `Etebar` | decimal(9) | خیر | — | ((0)) |
| `CheckAddToMandeh` | bit(1) | خیر | — | ((0)) |
| `MaxFaktorTasvieNashode` | int(4) | خیر | — | ((0)) |
| `MaxSarCheck` | int(4) | خیر | — | ((0)) |
| `MaxCheckPassNashode` | decimal(9) | خیر | — | ((0)) |
| `CreditCheckType` | int(4) | خیر | — | ((1)) |
| `MaxOpenTime` | nvarchar(20) | بله | — |  |
| `BlockResult` | nvarchar(4000) | بله | — |  |

### `Sys_MoeinId` — 7 ردیف (تخمین)
- کلیدها: PK_Sys_MoeinId(PK)=ID
- FK `FK_Sys_MoeinId_Moein`: MoeinId → Moein.MoeinID
- FK `FK_Sys_MoeinId_osystems`: SysId → osystems.rdf_system
- FK `FK_Sys_MoeinId_sys_users`: UserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `MoeinId` | bigint(8) | خیر | — |  |
| `SysId` | int(4) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |

### `TableChanges` — 1158 ردیف (تخمین)
- کلیدها: PK_TableChanges(PK)=RowID
- FK `<text 33>`: KindID → TableChangesKinds.KindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `KindID` | int(4) | خیر | — |  |
| `BaseKey` | bigint(8) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(20) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `Comment` | ntext(16) | بله | — |  |
| `DescriptionOfChanges` | nvarchar(-1) | بله | — |  |

### `TableChangesKinds` — 25 ردیف (تخمین)
- کلیدها: PK_TableChangesKinds(PK)=KindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `KindID` | int(4) | خیر | — |  |
| `KindName` | nvarchar(1000) | خیر | — |  |

### `TableName` — 4 ردیف (تخمین)
- کلیدها: PK_TableName(PK)=TableID
- FK `FK_TableName_TableStatus`: StatusID → TableStatus.StatusID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `TableID` | int(4) | خیر | بله |  |
| `TableName` | nvarchar(1000) | خیر | — |  |
| `StatusID` | int(4) | خیر | — |  |
| `PersainTableName` | nvarchar(1000) | بله | — |  |

### `TableStatus` — 4 ردیف (تخمین)
- کلیدها: PK_TableStatus(PK)=StatusID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `StatusID` | int(4) | خیر | بله |  |
| `NameStatus` | nvarchar(1000) | خیر | — |  |

### `TabletCustomer` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id
- FK `<text 32>`: shmo → CUSTOMERS.SHMO
- FK `<text 34>`: vis_rdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `vis_rdf` | int(4) | بله | — |  |
| `shmo` | int(4) | بله | — | (NULL) |
| `create_date` | varchar(10) | خیر | — |  |
| `birth_date` | varchar(10) | خیر | — |  |
| `name` | nvarchar(1000) | خیر | — |  |
| `melli_code` | nvarchar(200) | خیر | — |  |
| `tell1` | nvarchar(400) | بله | — |  |
| `tell2` | nvarchar(400) | بله | — |  |
| `cell` | nvarchar(400) | بله | — |  |
| `address` | nvarchar(1000) | خیر | — |  |
| `sharh` | nvarchar(1000) | بله | — |  |
| `estijari` | bit(1) | خیر | — |  |
| `owners_count` | int(4) | خیر | — |  |
| `metraj_shop` | decimal(9) | خیر | — |  |
| `metraj_yakhchal` | decimal(9) | خیر | — |  |
| `yakhchal_count` | int(4) | خیر | — |  |
| `tablo` | nvarchar(1000) | بله | — |  |
| `sabeghe` | int(4) | خیر | — |  |
| `Lat` | float(8) | بله | — |  |
| `Lng` | float(8) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `IsEdit` | bit(1) | خیر | — | ((0)) |
| `DoneSave` | bit(1) | بله | — | ((0)) |
| `ProvinceID` | int(4) | بله | — |  |
| `CityID` | int(4) | بله | — |  |
| `RegionID` | int(4) | بله | — |  |
| `MasirID` | int(4) | بله | — |  |
| `group_rdf` | int(4) | بله | — |  |
| `QuarterID` | int(4) | بله | — |  |

### `TaeediehErsalFactor` — 0 ردیف (تخمین)
- کلیدها: PK_TaeediehErsalFactor_1(PK)=Shfacfo
- FK `<text 32>`: UserID → sys_users.user_id
- FK `<text 31>`: RdfDriver → visitors.vis_rdf
- FK `<text 32>`: RdfMamoorMotalebat → visitors.vis_rdf
- FK `<text 32>`: RdfMamorPakhsh → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Shfacfo` | bigint(8) | خیر | — |  |
| `DeliverDate` | varchar(10) | خیر | — |  |
| `RdfMamoorMotalebat` | int(4) | بله | — |  |
| `RdfDriver` | int(4) | بله | — |  |
| `RdfMamorPakhsh` | int(4) | بله | — |  |
| `Receiver` | nvarchar(300) | بله | — |  |
| `TedadVahedKarton` | int(4) | بله | — |  |
| `TedadKartonFaree` | int(4) | بله | — |  |
| `TedadKartonMotefareghe` | int(4) | بله | — |  |
| `Description` | nvarchar(1000) | بله | — |  |
| `BasteBandi` | varchar(10) | خیر | — |  |
| `Lat` | float(8) | بله | — |  |
| `Lng` | float(8) | بله | — |  |
| `SignatureImage` | varbinary(-1) | بله | — |  |
| `UserID` | int(4) | بله | — |  |

### `TafifFlag` — 4 ردیف (تخمین)
- کلیدها: PK_TafifFlag(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `NameFlag` | nvarchar(100) | خیر | — |  |

### `Tafsil` — 2696 ردیف (تخمین)
- کلیدها: PK_Tafsil(PK)=TafsilID
- FK `FK_Tafsil_GroupTafsil`: TafsilGroupId → GroupTafsil.GroupID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `TafsilID` | bigint(8) | خیر | بله |  |
| `TafsilCode` | nchar(12) | خیر | — |  |
| `Name` | nvarchar(2000) | خیر | — |  |
| `FullName` | nvarchar(2000) | بله | — | (N'--') |
| `Active` | bit(1) | بله | — | ((1)) |
| `AddToDoc` | bit(1) | بله | — |  |
| `Man` | money(8) | بله | — |  |
| `IsEdit` | bit(1) | خیر | — | ((0)) |
| `TafsilGroupId` | bigint(8) | خیر | — |  |

### `TarazHistory` — 0 ردیف (تخمین)
- کلیدها: PK_TarazHistory(PK)=RowID
- FK `FK_TarazHistory_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `sh_taraz` | int(4) | خیر | — |  |
| `date` | nvarchar(20) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |
| `CarName` | nvarchar(1000) | بله | — |  |
| `MamoorPakhshName` | nvarchar(1000) | بله | — |  |
| `DriverName` | nvarchar(1000) | بله | — |  |
| `Sharh` | nvarchar(1000) | خیر | — |  |
| `SumVahed` | decimal(9) | خیر | — |  |
| `SumJoz` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `TimeServer` | nvarchar(100) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |

### `Tax` — 0 ردیف (تخمین)
- کلیدها: PK__Tax__3214EC272BC0CF75(PK)=ID
- FK `FK_Tax_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Year` | nvarchar(100) | خیر | — |  |
| `StartPrice` | money(8) | خیر | — |  |
| `EndPrice` | money(8) | خیر | — |  |
| `Percent` | float(8) | خیر | — |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `Description` | nvarchar(1000) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `TaxExemptionType` — 11 ردیف (تخمین)
- کلیدها: PK_TaxExemptionType(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ExemptionType` | nvarchar(200) | خیر | — |  |

### `TaxSystemLog` — 0 ردیف (تخمین)
- کلیدها: PK_TaxSystemLog(PK)=Rdf
- FK `FK_TaxSystemLog_ActNames`: DocumentType → ActNames.ActID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Rdf` | bigint(8) | خیر | بله |  |
| `DocumentNumber` | bigint(8) | خیر | — |  |
| `DocumentType` | int(4) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |
| `ChangeDate` | nvarchar(100) | خیر | — |  |
| `ChangeTime` | nvarchar(100) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `TaxUniqueID` | nvarchar(100) | خیر | — |  |
| `Uid` | nvarchar(1000) | بله | — |  |
| `RefrenceNumber` | nvarchar(1000) | بله | — |  |
| `FiscalId` | nvarchar(100) | بله | — |  |

### `TblMostSalesGoods` — 0 ردیف (تخمین)
- کلیدها: PK_TblMostSalesGoods(PK)=MostSalesGoodsID
- FK `<text 30>`: Shka → inventory.shka
- FK `<text 30>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MostSalesGoodsID` | int(4) | خیر | بله |  |
| `Shka` | bigint(8) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `TblNodesGozareshRuzaneh` — 17 ردیف (تخمین)
- کلیدها: PK_TblNodesGozareshRuzaneh(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | — |  |
| `NodeName` | nvarchar(100) | خیر | — |  |
| `Type` | int(4) | بله | — |  |

### `TblReminder` — 0 ردیف (تخمین)
- کلیدها: PK_TblReminder(PK)=RowID
- FK `FK_TblReminder_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | bigint(8) | خیر | بله |  |
| `UserID` | int(4) | بله | — |  |
| `DateCreate` | nvarchar(20) | بله | — |  |
| `TimeCreate` | nvarchar(16) | بله | — |  |
| `Description` | nvarchar(-1) | بله | — |  |
| `AlarmDate` | nvarchar(20) | بله | — |  |
| `AlarmTime` | nvarchar(16) | بله | — |  |
| `Complete` | bit(1) | بله | — |  |

### `TellBook` — 2732 ردیف (تخمین)
- کلیدها: PK_TellBook(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Moname` | nvarchar(1000) | خیر | — |  |
| `Address1` | nvarchar(1000) | خیر | — |  |
| `Address2` | nvarchar(1000) | خیر | — |  |
| `Tell1` | nvarchar(1000) | خیر | — |  |
| `Tell2` | nvarchar(1000) | خیر | — |  |
| `Tell3` | nvarchar(1000) | خیر | — |  |
| `Tell4` | nvarchar(1000) | خیر | — |  |
| `Cell1` | nvarchar(1000) | خیر | — |  |
| `Cell2` | nvarchar(1000) | خیر | — |  |
| `Cell3` | nvarchar(1000) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `LastEditedUser` | int(4) | خیر | — |  |
| `shmo` | int(4) | خیر | — |  |

### `TempPsId` — 0 ردیف (تخمین)
- کلیدها: PK_TempPsId(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `PsIdNew` | bigint(8) | خیر | — |  |
| `PsIdOld` | bigint(8) | خیر | — |  |

### `TemplateDaryaftCheque` — 0 ردیف (تخمین)
- کلیدها: PK_TemplateDaryaftCheque(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `BankName` | nvarchar(1000) | خیر | — |  |
| `Shmo` | bigint(8) | خیر | — |  |
| `Mab` | money(8) | خیر | — |  |
| `Shhesab` | nvarchar(100) | خیر | — |  |
| `Shobe` | nvarchar(1000) | خیر | — |  |
| `ShSerial` | nvarchar(100) | خیر | — |  |
| `SarDate` | nvarchar(20) | خیر | — |  |
| `DarDate` | nvarchar(20) | خیر | — |  |
| `Desc` | nvarchar(1000) | خیر | — |  |
| `Ghno` | int(4) | خیر | — |  |
| `Shahrestan` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `Shfacfo` | bigint(8) | خیر | — |  |
| `RdfVisitor` | int(4) | خیر | — |  |
| `RdfMP` | int(4) | خیر | — |  |
| `RdfMM` | int(4) | خیر | — |  |
| `RdfDriver` | int(4) | خیر | — |  |
| `Moname` | nvarchar(1000) | خیر | — |  |
| `ID` | int(4) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |
| `Rdf_` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `ShenaseSayad` | nvarchar(100) | بله | — |  |
| `RegistrationInquiry` | bit(1) | خیر | — | ((0)) |
| `MoeinId` | bigint(8) | بله | — |  |
| `CheckTypeID` | int(4) | خیر | — |  |

### `TemplatePardakhtCheque` — 0 ردیف (تخمین)
- کلیدها: PK_TemplatePardakhtCheque(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Shmo` | bigint(8) | خیر | — |  |
| `Mab` | money(8) | خیر | — |  |
| `ShSerial` | nvarchar(100) | خیر | — |  |
| `SarDate` | nvarchar(20) | خیر | — |  |
| `ParDate` | nvarchar(20) | خیر | — |  |
| `Desc` | nvarchar(1000) | خیر | — |  |
| `Ghno` | int(4) | خیر | — |  |
| `SysID` | int(4) | خیر | — |  |
| `Moname` | nvarchar(1000) | خیر | — |  |
| `ID` | int(4) | خیر | — |  |
| `DoneDate` | nvarchar(20) | خیر | — |  |
| `RDF` | bigint(8) | خیر | — |  |
| `BankRdf` | bigint(8) | خیر | — |  |
| `FromKharid` | bit(1) | بله | — |  |
| `Rdf_` | int(4) | بله | — |  |
| `Edit` | bit(1) | بله | — |  |
| `Ebtal` | bit(1) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `ShenaseSayad` | nvarchar(100) | بله | — |  |
| `RegistrationInquiry` | bit(1) | خیر | — | ((0)) |
| `MoeinId` | bigint(8) | بله | — |  |

### `TemproryAccount` — 0 ردیف (تخمین)
- کلیدها: PK_TemproryAccount(PK)=RowID
- FK `FK_TemproryAccount_Moein`: MoeinId → Moein.MoeinID
- FK `FK_TemproryAccount_Tafsil`: TafsilCode → Tafsil.TafsilID
- FK `<text 34>`: DocID → Document.DocID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `DocID` | bigint(8) | خیر | — |  |
| `DocNumber` | int(4) | خیر | — |  |
| `Bed` | decimal(9) | خیر | — |  |
| `Bes` | decimal(9) | خیر | — |  |
| `Desc` | nvarchar(1000) | خیر | — |  |
| `TafsilCode` | bigint(8) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — |  |
| `kolName` | nvarchar(600) | خیر | — | ('') |
| `kolCode` | nvarchar(6) | خیر | — | ('') |
| `moeinName` | nvarchar(600) | خیر | — | ('') |
| `moeinCode` | nvarchar(6) | خیر | — | ('') |
| `tafsilName` | nvarchar(600) | بله | — |  |
| `codeTafsil` | nvarchar(12) | بله | — |  |

### `TerminalCompanyPos` — 8 ردیف (تخمین)
- کلیدها: PK_TerminalCompanyPos(PK)=TerminalCompanyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `TerminalCompanyID` | int(4) | خیر | بله |  |
| `TerminalCompanyName` | nvarchar(4000) | خیر | — |  |

### `TerminalPos` — 0 ردیف (تخمین)
- کلیدها: PK_TerminalPos(PK)=TerminalID
- FK `FK_TerminalPos_sys_users`: UserID → sys_users.user_id
- FK `<text 33>`: TerminalCompanyID → TerminalCompanyPos.TerminalCompanyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `TerminalID` | int(4) | خیر | بله |  |
| `TerminalCompanyID` | int(4) | خیر | — |  |
| `TerminalName` | nvarchar(4000) | خیر | — |  |
| `TerminalPort` | varchar(500) | خیر | — |  |
| `TerminalIP` | varchar(500) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(20) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `TerminalNumber` | varchar(500) | بله | — |  |
| `AcceptorId` | varchar(500) | بله | — |  |
| `SerialNo` | varchar(500) | بله | — |  |

### `ToSite` — 0 ردیف (تخمین)
- کلیدها: PK_ToSite(PK)=RowId
- FK `FK_ToSite_ToSiteKind`: Kind → ToSiteKind.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | bigint(8) | خیر | بله |  |
| `ID` | bigint(8) | خیر | — |  |
| `Kind` | int(4) | خیر | — |  |
| `Sended` | bit(1) | خیر | — |  |

### `ToSiteKind` — 6 ردیف (تخمین)
- کلیدها: PK_ToSiteKind(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Title` | nvarchar(200) | خیر | — |  |

### `Transactions` — 5 ردیف (تخمین)
- کلیدها: PK_Transactions(PK)=ID, ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ID` | int(4) | خیر | بله |  |
| `tci` | varbinary(1000) | بله | — |  |
| `tci` | varbinary(1000) | بله | — |  |
| `nvalue` | nvarchar(1000) | بله | — |  |
| `nvalue` | nvarchar(1000) | بله | — |  |
| `iv` | varbinary(1000) | بله | — |  |
| `iv` | varbinary(1000) | بله | — |  |

### `TransferBasic` — 0 ردیف (تخمین)
- کلیدها: PK_TransferBasic(PK)=Id
- FK `<text 29>`: Kind → TransferKind.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `Ref_Num` | bigint(8) | خیر | — |  |
| `Kind` | int(4) | خیر | — |  |

### `TransferKind` — 16 ردیف (تخمین)
- کلیدها: PK_TransferKind(PK)=Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | — |  |
| `Name` | nvarchar(100) | خیر | — |  |

### `TransferSanad` — 0 ردیف (تخمین)
- کلیدها: PK_TransferSanad(PK)=Id
- FK `<text 29>`: Kind → TransferKind.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `Ref_Num` | bigint(8) | خیر | — |  |
| `Kind` | int(4) | خیر | — |  |
| `New_Num` | bigint(8) | خیر | — | ((0)) |

### `UNITS` — 14 ردیف (تخمین)
- کلیدها: PK_UNITS(PK)=RDF
- FK `FK_UNITS_UNITS`: ParentUnitId → UNITS.RDF

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RDF` | int(4) | خیر | بله |  |
| `UNIT_NAME` | varchar(30) | خیر | — |  |
| `ParentUnitId` | int(4) | بله | — |  |
| `BarcodeId` | int(4) | بله | — |  |
| `ParentCapacity` | int(4) | بله | — |  |
| `UnitLevel` | int(4) | بله | — |  |
| `UnitCode` | int(4) | خیر | — | ((0)) |

### `UnTaeediehErsalFactor` — 0 ردیف (تخمین)
- کلیدها: PK_UnTaeediehErsalFactor(PK)=RowId
- FK `<text 44>`: IdReasons → ReasonForRevocation.RevocationID
- FK `<text 33>`: VisRdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | int(4) | خیر | بله |  |
| `shfac` | bigint(8) | خیر | — |  |
| `shtaraz` | bigint(8) | خیر | — |  |
| `VisRdf` | int(4) | خیر | — |  |
| `IdReasons` | int(4) | خیر | — |  |
| `Date` | char(10) | خیر | — |  |
| `Time` | char(8) | خیر | — |  |
| `Desceription` | nvarchar(1000) | خیر | — |  |
| `lat` | float(8) | خیر | — |  |
| `lng` | float(8) | خیر | — |  |

### `UpdateHistory` — 0 ردیف (تخمین)
- کلیدها: PK_UpdateHistory(PK)=ID, RowID
- FK `FK_UpdateHistory_UpdateKind`: UpdateKindID → UpdateKind.ID
- FK `FK_UpdateHistory_User`: UpdateKindID → UpdateKind.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `ID` | int(4) | خیر | بله |  |
| `Time` | datetime(8) | خیر | — |  |
| `TableName` | varchar(200) | خیر | — |  |
| `Version` | nvarchar(100) | خیر | — |  |
| `UpdateKindID` | int(4) | خیر | — |  |
| `ReleaseDay` | nvarchar(20) | خیر | — |  |
| `RefID` | bigint(8) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `UpdateSystemDateTime` | datetime(8) | خیر | — |  |
| `UpdateServerDateTime` | datetime(8) | خیر | — |  |
| `UpdateShamsiDate` | nchar(20) | خیر | — |  |
| `OldValues` | text(16) | خیر | — |  |

### `UpdateKind` — 20 ردیف (تخمین)
- کلیدها: PK_UpdateKind(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Kind` | nvarchar(300) | خیر | — |  |
| `Description` | nvarchar(600) | بله | — |  |

### `UserAnbar` — 0 ردیف (تخمین)
- کلیدها: PK_UserAnbar(PK)=RowID
- FK `FK_UserAnbar_anbars`: AnbarID → anbars.rdf_anbar
- FK `FK_UserAnbar_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `AnbarID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |

### `UserAppAccess` — 93 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `FK_AppAccess_UserAppAccess`: AppAccessID → AppAccess.ID
- FK `FK_UserAppAccess_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `AppAccessID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |

### `UserCustomDataGrid` — 0 ردیف (تخمین)
- کلیدها: PK_UserCustomDataGrid(PK)=RowID
- FK `<text 31>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `FormName` | nvarchar(800) | خیر | — |  |
| `DataGridName` | nvarchar(800) | خیر | — |  |
| `UserSetting` | nvarchar(-1) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `UserFavorites` — 3 ردیف (تخمین)
- کلیدها: PK_UserFavorites(PK)=RowID
- FK `FK_UserFavorites_Menu`: MenuID → Menu.MenuID
- FK `FK_UserFavorites_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `MenuID` | int(4) | خیر | — |  |

### `UserFieldPermission` — 1415 ردیف (تخمین)
- کلیدها: PK_UserFieldPermission(PK)=UserFieldPermissionId
- FK `FK_UserFieldPermission_Field`: FieldId → Field.FieldId
- FK `<text 33>`: PermissionId → Permission.PermissionId
- FK `<text 32>`: user_id → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `UserFieldPermissionId` | int(4) | خیر | بله |  |
| `user_id` | int(4) | خیر | — |  |
| `PermissionId` | int(4) | خیر | — |  |
| `FieldId` | int(4) | خیر | — |  |

### `UserFormPermission` — 2047 ردیف (تخمین)
- کلیدها: PK_UserFormPermission(PK)=UserFormPermissionId
- FK `FK_UserFormPermission_Form`: FormId → Form.FormId
- FK `<text 32>`: PermissionId → Permission.PermissionId
- FK `<text 31>`: user_id → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `UserFormPermissionId` | int(4) | خیر | بله |  |
| `user_id` | int(4) | خیر | — |  |
| `PermissionId` | int(4) | خیر | — |  |
| `FormId` | int(4) | خیر | — |  |

### `UserKind` — 0 ردیف (تخمین)
- کلیدها: PK_UserKind(PK)=UserKindID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `UserKindID` | int(4) | خیر | بله |  |
| `UserKindName` | nvarchar(100) | بله | — |  |

### `UserPersonalizationForColor` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=PersonalizationColorID
- FK `<text 44>`: ControlColorID → ControlColors.ControlColorID
- FK `<text 43>`: ControlNameID → ControlNames.ControlID
- FK `<text 40>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `PersonalizationColorID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `ControlNameID` | int(4) | خیر | — |  |
| `ControlColorID` | int(4) | خیر | — |  |
| `R` | int(4) | خیر | — |  |
| `G` | int(4) | خیر | — |  |
| `B` | int(4) | خیر | — |  |

### `UserPersonalizationForFont` — 2 ردیف (تخمین)
- کلیدها: <text 29>(PK)=PersonalizationFontID
- FK `<text 42>`: ControlNameID → ControlNames.ControlID
- FK `<text 39>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `PersonalizationFontID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `ControlNameID` | int(4) | خیر | — |  |
| `FontSize` | int(4) | خیر | — |  |

### `UserPos` — 0 ردیف (تخمین)
- کلیدها: PK_UserPos(PK)=UserPosID
- FK `FK_UserPos_BANK`: BankPoseID → BankPos.BankPosID
- FK `FK_UserPos_sys_users`: UserID → sys_users.user_id
- FK `FK_UserPos_sys_users1`: ConfirmedUserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `UserPosID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `BankPoseID` | int(4) | خیر | — |  |
| `DateServer` | nvarchar(20) | خیر | — |  |
| `TimeServer` | nvarchar(20) | خیر | — |  |
| `ConfirmedUserID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `isDefault` | bit(1) | خیر | — | ((0)) |

### `UserRole` — 0 ردیف (تخمین)
- کلیدها: PK_UserRole(PK)=UserRoleId
- FK `FK_UserRole_Roles`: RoleId → Roles.id
- FK `FK_UserRole_sys_users`: user_id → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `UserRoleId` | int(4) | خیر | بله |  |
| `user_id` | int(4) | خیر | — |  |
| `RoleId` | int(4) | خیر | — |  |

### `UserWorkhouses` — 1 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `FK_UserWorkhouses_sys_users`: UserID → sys_users.user_id
- FK `FK_UserWorkhouses_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | خیر | — |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `IsConnect` | bit(1) | بله | — |  |

### `UsersPrivacy` — 6 ردیف (تخمین)
- کلیدها: PK_UsersPrivacy(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | bigint(8) | خیر | بله |  |
| `SysUserID` | int(4) | خیر | — |  |
| `Type` | smallint(2) | خیر | — |  |
| `MaxPercentValue` | float(8) | بله | — |  |
| `MaxPriceValue` | money(8) | بله | — |  |

### `VacationDays` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ShamsiDate` | nchar(20) | بله | — |  |
| `Date` | datetime(8) | بله | — |  |

### `Variety` — 1803 ردیف (تخمین)
- کلیدها: PK_Variety(PK)=VarietyID
- FK `FK_Variety_inventory`: Shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `VarietyID` | int(4) | خیر | بله |  |
| `Shka` | bigint(8) | خیر | — |  |
| `VarietyName` | nvarchar(1000) | خیر | — | ('') |
| `CustomerPrice` | money(8) | خیر | — | ((0)) |
| `Show` | bit(1) | خیر | — | ((0)) |

### `VarietyTransferHeader` — 0 ردیف (تخمین)
- کلیدها: PK_VarietyTransferHeader(PK)=RowID, DocID
- FK `<text 31>`: AnbarID → anbars.rdf_anbar
- FK `<text 34>`: Shka → inventory.shka
- FK `<text 38>`: VarietyIDSource → Variety.VarietyID
- FK `<text 38>`: VarietyIDTarget → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `DocID` | int(4) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `AnbarID` | int(4) | خیر | — |  |
| `VarietyIDSource` | int(4) | خیر | — |  |
| `VarietyIDTarget` | int(4) | خیر | — |  |
| `Descriptions` | nvarchar(-1) | بله | — |  |
| `TedadVahed` | decimal(9) | خیر | — | ((0)) |
| `TedadJoz` | int(4) | خیر | — | ((0)) |
| `isCenceled` | bit(1) | خیر | — | ((0)) |
| `Date` | nvarchar(20) | خیر | — |  |

### `Version` — 8 ردیف (تخمین)
- کلیدها: PK_Version(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | — |  |
| `VersionName` | nvarchar(1000) | خیر | — |  |
| `Explain` | nvarchar(-1) | خیر | — |  |

### `Visit` — 0 ردیف (تخمین)
- کلیدها: PK_Visit(PK)=VisitID
- FK `FK_Visit_CUSTOMERS`: Shmo → CUSTOMERS.SHMO
- FK `FK_Visit_visitors`: VisRdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `VisitID` | bigint(8) | خیر | بله |  |
| `VisRdf` | int(4) | خیر | — |  |
| `Shmo` | int(4) | خیر | — |  |
| `Duration` | int(4) | خیر | — |  |
| `Created` | bigint(8) | خیر | — |  |
| `Sent` | bigint(8) | بله | — |  |
| `Description` | nvarchar(2000) | بله | — |  |
| `SentLng` | float(8) | بله | — |  |
| `SentLat` | float(8) | بله | — |  |
| `SaveLat` | float(8) | بله | — |  |
| `SaveLng` | float(8) | بله | — |  |
| `SignatureImage` | varbinary(-1) | بله | — |  |
| `DateCreated` | char(10) | بله | — |  |
| `DateSent` | char(10) | بله | — |  |
| `TimeCreated` | char(8) | بله | — |  |
| `TimeSent` | char(8) | بله | — |  |

### `VoipRegister` — 0 ردیف (تخمین)
- کلیدها: PK_VoipRegister(PK)=userID
- FK `FK_VoipRegister_sys_users`: userID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `regID` | int(4) | خیر | بله |  |
| `userID` | int(4) | خیر | — |  |
| `displayName` | nvarchar(400) | بله | — |  |
| `userName` | nvarchar(400) | بله | — |  |
| `authenticationId` | nvarchar(400) | بله | — |  |
| `registerPassword` | nvarchar(400) | بله | — |  |
| `domainHost` | nvarchar(100) | بله | — |  |
| `domainPort` | bigint(8) | بله | — |  |

### `Week` — 63 ردیف (تخمین)
- کلیدها: PK_Week(PK)=WeekID
- FK `FK_Week_Month`: MonthID → Month.MonthID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `WeekID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(100) | بله | — |  |
| `MonthID` | int(4) | خیر | — |  |
| `StartDate` | nvarchar(20) | بله | — |  |
| `EndDate` | nvarchar(20) | بله | — |  |

### `WhatsappTable` — 0 ردیف (تخمین)
- کلیدها: PK_WhatsappTable(PK)=Rdf
- FK `FK_whatsapp_table_sys_users`: UserId → sys_users.user_id
- FK `FK_whatsapp_table_sys_users1`: DeleteUserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Rdf` | int(4) | خیر | بله |  |
| `TextMsg` | nvarchar(8000) | خیر | — |  |
| `PhoneNumber` | nvarchar(100) | خیر | — |  |
| `UserId` | int(4) | بله | — |  |
| `status` | int(4) | خیر | — |  |
| `DoneDate` | char(10) | بله | — |  |
| `Deleted` | int(4) | بله | — |  |
| `DeleteUserId` | int(4) | بله | — |  |
| `Uid` | nvarchar(100) | بله | — |  |
| `ErrorComment` | nvarchar(1000) | بله | — |  |
| `Title` | nvarchar(1000) | بله | — |  |
| `SysId` | int(4) | بله | — |  |

### `Workhouse` — 1 ردیف (تخمین)
- کلیدها: PK_PaymentLocation(PK)=ID
- FK `FK_Workhouse_sys_users`: InsertBy → sys_users.user_id
- FK `FK_Workhouse_sys_users1`: UpdateBy → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseName` | nvarchar(400) | خیر | — |  |
| `WorkhouseCode` | nvarchar(400) | بله | — |  |
| `AgreementCode` | nvarchar(400) | بله | — |  |
| `EmployerName` | nvarchar(200) | بله | — |  |
| `WorkhouseAddress` | nvarchar(1000) | بله | — |  |
| `Description` | nvarchar(1000) | بله | — |  |
| `InsertBy` | int(4) | بله | — |  |
| `InsertSystemDateTime` | datetime(8) | بله | — |  |
| `InsertServerDateTime` | datetime(8) | بله | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `WorkhouseSettings` — 0 ردیف (تخمین)
- کلیدها: PK_WorkhouseSettings(PK)=ID
- FK `<text 30>`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `<text 30>` | bit(1) | بله | — |  |
| `InsuranceEndOfWorkBenefits` | bit(1) | بله | — |  |
| `InsuranceHousingAllowance` | bit(1) | بله | — |  |
| `InsuranceNewYearGift` | bit(1) | بله | — |  |
| `InsuranceTransportation` | bit(1) | بله | — |  |
| `InsuranceHireAllowance` | bit(1) | بله | — |  |
| `InsuranceMealStipend` | bit(1) | بله | — |  |
| `InsuranceCreditOfLeave` | bit(1) | بله | — |  |
| `<text 29>` | bit(1) | بله | — |  |
| `InsuranceRewardPrice` | bit(1) | بله | — |  |
| `InsuranceBadWeatherAllowance` | bit(1) | بله | — |  |
| `InsuranceOtherBenefits` | bit(1) | بله | — |  |
| `InsuranceChildAllowance` | bit(1) | بله | — |  |
| `TaxEndOfWorkBenefitsBase` | bit(1) | بله | — |  |
| `TaxEndOfWorkBenefits` | bit(1) | بله | — |  |
| `TaxHousingAllowance` | bit(1) | بله | — |  |
| `TaxNewYearGift` | bit(1) | بله | — |  |
| `TaxTransportation` | bit(1) | بله | — |  |
| `TaxHireAllowance` | bit(1) | بله | — |  |
| `TaxMealStipend` | bit(1) | بله | — |  |
| `TaxCreditOfLeave` | bit(1) | بله | — |  |
| `TaxSupervisionAllowance` | bit(1) | بله | — |  |
| `TaxRewardPrice` | bit(1) | بله | — |  |
| `TaxBadWeatherAllowance` | bit(1) | بله | — |  |
| `TaxOtherBenefits` | bit(1) | بله | — |  |
| `TaxChildAllowance` | bit(1) | بله | — |  |
| `InsuranceOvertime` | bit(1) | بله | — |  |
| `TaxOvertime` | bit(1) | بله | — |  |
| `InsuranceNightWork` | bit(1) | بله | — |  |
| `TaxNightWork` | bit(1) | بله | — |  |
| `InsuranceMission` | bit(1) | بله | — |  |
| `TaxMission` | bit(1) | بله | — |  |
| `InsuranceShiftWork` | bit(1) | بله | — |  |
| `TaxShiftWork` | bit(1) | بله | — |  |
| `InsuranceWorkInVacation` | bit(1) | بله | — |  |
| `TaxWorkInVacation` | bit(1) | بله | — |  |
| `PayrollDeduction` | money(8) | بله | — |  |
| `UpRound` | bit(1) | بله | — |  |
| `DownRound` | bit(1) | بله | — |  |
| `RoundDigitCount` | tinyint(1) | بله | — |  |
| `InsuranceMarriagePrivilege` | bit(1) | بله | — |  |
| `TaxMarriagePrivilege` | bit(1) | بله | — |  |
| `CalcInsuranceInEdit` | bit(1) | خیر | — | ((0)) |
| `CalcTaxInEdit` | bit(1) | خیر | — | ((0)) |
| `SplitPersonnelClaim` | bit(1) | خیر | — | ((0)) |
| `EndOfWorkBenefitBaseDaily` | bit(1) | خیر | — | ((0)) |
| `ChildAllowanceDaily` | bit(1) | خیر | — | ((0)) |
| `HousingAllowanceDaily` | bit(1) | خیر | — | ((0)) |
| `BadWeatherAllowanceDaily` | bit(1) | خیر | — | ((0)) |
| `MealStipendDaily` | bit(1) | خیر | — | ((0)) |
| `HireAllowanceDaily` | bit(1) | خیر | — | ((0)) |
| `MarriagePrivilegeDaily` | bit(1) | خیر | — | ((0)) |
| `SupervisionAllowanceDaily` | bit(1) | خیر | — | ((0)) |
| `OtherBenefitsDaily` | bit(1) | خیر | — | ((0)) |
| `EndOfWorkBenefitsDaily` | bit(1) | خیر | — | ((0)) |

### `WorkingConvention` — 0 ردیف (تخمین)
- کلیدها: PK_WorkingConvention(PK)=Id
- FK `<text 37>`: CooperationTypeID → CooperationTypes.ID
- FK `<text 35>`: PersonnelStatusID → EmployeeStatus.ID
- FK `<text 30>`: InsuranceTypeID → Insurance.ID
- FK `FK_WorkingConvention_Jobs`: JobId → Jobs.ID
- FK `<text 47>`: NewYearGiftCalculationTypeID → NewYearGiftCalculationType.ID
- FK `<text 30>`: PersonnelId → Personnel.ID
- FK `<text 30>`: ShiftTypeID → ShiftType.ID
- FK `<text 30>`: InsertBy → sys_users.user_id
- FK `<text 37>`: TaxExemptionTypeID → TaxExemptionType.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Id` | int(4) | خیر | بله |  |
| `PersonnelId` | int(4) | خیر | — |  |
| `StartDate` | datetime(8) | خیر | — |  |
| `StartDateShamsi` | nchar(100) | خیر | — |  |
| `EndDate` | datetime(8) | خیر | — |  |
| `EndDateShamsi` | nchar(100) | خیر | — |  |
| `JobId` | int(4) | بله | — |  |
| `ConstantDailySalary` | money(8) | خیر | — |  |
| `ConstantHourlySalary` | money(8) | خیر | — |  |
| `MonthlyWorkingHours` | float(8) | خیر | — |  |
| `BaseMonthlySalary` | money(8) | خیر | — |  |
| `MonthlyAgreementTime` | smallint(2) | خیر | — |  |
| `CooperationTypeID` | int(4) | خیر | — |  |
| `PersonnelStatusID` | int(4) | خیر | — |  |
| `ShamsiHireDate` | nchar(100) | خیر | — |  |
| `EndOfWorkBenefitsBase` | money(8) | خیر | — |  |
| `NewYearGiftCalculationTypeID` | int(4) | خیر | — |  |
| `ChildAllowancePrice` | money(8) | خیر | — |  |
| `HousingAllowancePrice` | money(8) | خیر | — |  |
| `BadWeatherAllowancePrice` | money(8) | خیر | — |  |
| `MealStipendPrice` | money(8) | خیر | — |  |
| `HireAllowance` | money(8) | خیر | — |  |
| `IsIncludedInsurance` | bit(1) | خیر | — |  |
| `InsuranceTypeID` | int(4) | خیر | — |  |
| `WorkerInsurancePercent` | float(8) | خیر | — |  |
| `EmployerInsurancePercent` | float(8) | خیر | — |  |
| `UnemploymentInsurancePercent` | float(8) | خیر | — |  |
| `MarriagePrivilege` | money(8) | خیر | — |  |
| `TaxExemptionTypeID` | int(4) | خیر | — |  |
| `SupervisionAllowance` | money(8) | خیر | — |  |
| `OtherBenefits` | money(8) | خیر | — |  |
| `DailyMissionPrice` | money(8) | خیر | — |  |
| `OvertimePrice` | money(8) | خیر | — |  |
| `NightWorkPrice` | money(8) | خیر | — |  |
| `MissionPrice` | money(8) | خیر | — |  |
| `WorkInVacationPrice` | money(8) | خیر | — |  |
| `ShiftTypeID` | int(4) | بله | — |  |
| `EndOfWorkBenefits` | money(8) | خیر | — |  |
| `WorkDeductionPrice` | money(8) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `PerformanceBondDescription` | nvarchar(1000) | خیر | — |  |
| `InsertBy` | int(4) | خیر | — |  |
| `InsertDate` | datetime(8) | خیر | — |  |
| `InsertShamsiDate` | nchar(20) | بله | — |  |

### `WorkingHours` — 0 ردیف (تخمین)
- کلیدها: PK_WorkingHours(PK)=ID
- FK `FK_WorkingHours_Personnel`: PersonnelID → Personnel.ID
- FK `FK_WorkingHours_sys_users`: InsertBy → sys_users.user_id
- FK `FK_WorkingHours_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_WorkingHours_Workhouse`: WorkhouseID → Workhouse.ID
- FK `<text 29>`: WorkingShiftID → WorkingShifts.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkhouseID` | int(4) | خیر | — |  |
| `PersonnelID` | int(4) | خیر | — |  |
| `ArrivalTime` | nchar(20) | خیر | — |  |
| `ExitTime` | nchar(20) | خیر | — |  |
| `WorkingDate` | date(3) | خیر | — |  |
| `WorkingShamsiDate` | nchar(20) | خیر | — |  |
| `ConfirmedOvertimes` | nchar(20) | بله | — |  |
| `ConfirmedDelays` | nchar(20) | بله | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `InsertBy` | int(4) | خیر | — |  |
| `InsertSystemDateTime` | datetime(8) | خیر | — |  |
| `InsertServerDateTime` | datetime(8) | خیر | — |  |
| `InsertShamsiDate` | nchar(20) | خیر | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — |  |
| `WorkingShiftID` | int(4) | بله | — |  |
| `IsOffDay` | bit(1) | بله | — |  |
| `IncompleteTraffic` | bit(1) | بله | — |  |
| `FractionWork` | bigint(8) | بله | — |  |
| `ApprovedOvertime` | bit(1) | بله | — |  |

### `WorkingHoursDetails` — 0 ردیف (تخمین)
- کلیدها: PK_WorkingHoursDetails(PK)=ID
- FK `<text 42>`: WorkingHoursID → WorkingHours.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkingHoursID` | int(4) | خیر | — |  |
| `ArrivalTime` | nchar(20) | بله | — |  |
| `ExitTime` | nchar(20) | بله | — |  |
| `ConfirmedArrival` | bigint(8) | بله | — |  |
| `ConfirmedExit` | bigint(8) | بله | — |  |
| `Presence` | nchar(20) | بله | — |  |
| `MissionID` | int(4) | بله | — |  |
| `LeaveID` | int(4) | بله | — |  |

### `WorkingShifts` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=ID
- FK `FK_WorkingShifts_sys_users`: InsertBy → sys_users.user_id
- FK `FK_WorkingShifts_sys_users1`: UpdateBy → sys_users.user_id
- FK `FK_WorkingShifts_Workhouse`: WorkhouseID → Workhouse.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `WorkingShift` | nvarchar(100) | خیر | — |  |
| `SaturdayArrivalTime` | nchar(20) | بله | — |  |
| `SaturdayExitTime` | nchar(20) | بله | — |  |
| `SaturdayIsDayOff` | bit(1) | خیر | — |  |
| `SundayArrivalTime` | nchar(20) | بله | — |  |
| `SundayExitTime` | nchar(20) | بله | — |  |
| `SundayIsDayOff` | bit(1) | خیر | — |  |
| `MondayArrivalTime` | nchar(20) | بله | — |  |
| `MondayExitTime` | nchar(20) | بله | — |  |
| `MondayIsDayOff` | bit(1) | خیر | — |  |
| `TuesdayArrivalTime` | nchar(20) | بله | — |  |
| `TuesdayExitTime` | nchar(20) | بله | — |  |
| `TuesdayIsDayOff` | bit(1) | خیر | — |  |
| `WednesdayArrivalTime` | nchar(20) | بله | — |  |
| `WednesdayExitTime` | nchar(20) | بله | — |  |
| `WednesdayIsDayOff` | bit(1) | خیر | — |  |
| `ThursdayArrivalTime` | nchar(20) | بله | — |  |
| `ThursdayExitTime` | nchar(20) | بله | — |  |
| `ThursdayIsDayOff` | bit(1) | خیر | — |  |
| `FridayArrivalTime` | nchar(20) | بله | — |  |
| `FridayExitTime` | nchar(20) | بله | — |  |
| `FridayIsDayOff` | bit(1) | خیر | — |  |
| `Description` | nvarchar(600) | بله | — |  |
| `InsertBy` | int(4) | خیر | — |  |
| `InsertSystemDateTime` | datetime(8) | خیر | — |  |
| `InsertServerDateTime` | datetime(8) | خیر | — |  |
| `InsertShamsiDate` | nchar(20) | خیر | — |  |
| `UpdateBy` | int(4) | بله | — |  |
| `UpdateSystemDateTime` | datetime(8) | بله | — |  |
| `UpdateServerDateTime` | datetime(8) | بله | — |  |
| `UpdateShamsiDate` | nchar(20) | بله | — |  |
| `UpdateVersion` | int(4) | خیر | — |  |
| `WorkhouseID` | int(4) | بله | — |  |
| `Active` | bit(1) | خیر | — |  |

### `Year` — 1 ردیف (تخمین)
- کلیدها: PK_Year(PK)=YearID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `YearID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(100) | بله | — |  |

### `ZamanBandiTasviehFactor` — 0 ردیف (تخمین)
- کلیدها: PK_ZamanBandiTasviehFactor(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `Name` | nvarchar(100) | بله | — |  |
| `TeadadRooz` | int(4) | بله | — |  |
| `Date` | char(10) | بله | — |  |
| `Time` | char(10) | بله | — |  |

### `ZirSarFaslDocuments` — 231 ردیف (تخمین)
- کلیدها: PK_ZirSarFaslDocuments(PK)=SanadNumber

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `SanadNumber` | int(4) | خیر | بله |  |
| `Kind` | int(4) | خیر | — |  |
| `date` | nvarchar(20) | خیر | — |  |
| `doneDate` | nvarchar(20) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `KarMozd` | decimal(9) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |

### `act_zirsarfasls` — 291 ردیف (تخمین)
- کلیدها: PK_act_zirsarfasls(PK)=rdf
- FK `FK_act_zirsarfasls_sys_users`: UserID → sys_users.user_id
- FK `<text 30>`: rdf_zirsarfasls → zirsarfasls.rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `date` | char(10) | خیر | — |  |
| `rdf_zirsarfasls` | int(4) | خیر | — |  |
| `dis` | nvarchar(-1) | خیر | — | ('ذکر نشده') |
| `bed` | money(8) | خیر | — | ((0)) |
| `bes` | money(8) | خیر | — | ((0)) |
| `kind` | int(4) | بله | — |  |
| `sanadno` | int(4) | بله | — |  |
| `DoneDate` | nvarchar(20) | بله | — |  |
| `TimeServer` | nvarchar(20) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `ShowInReport` | bit(1) | خیر | — | ((1)) |

### `anbarSanadType` — 17 ردیف (تخمین)
- کلیدها: PK_anbarSanadType(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `name` | nvarchar(100) | خیر | — |  |

### `anbars` — 1 ردیف (تخمین)
- کلیدها: PK_anbars(PK)=rdf_anbar
- FK `FK_anbars_Moein`: MoeinId → Moein.MoeinID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_anbar` | int(4) | خیر | بله |  |
| `name` | nvarchar(600) | خیر | — |  |
| `addre` | nvarchar(4000) | خیر | — | ('ذکر نشده') |
| `start_date` | nvarchar(100) | خیر | — |  |
| `tell1` | nvarchar(100) | خیر | — | ('ذکر نشده') |
| `tell2` | nvarchar(100) | خیر | — |  |
| `anbardar` | nvarchar(1000) | بله | — |  |
| `Active` | bit(1) | بله | — | ((1)) |
| `Base` | bit(1) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — | (NULL) |

### `b_az_mosh_sanad` — 47 ردیف (تخمین)
- کلیدها: PK_b_az_mosh_sanad(PK)=rdf
- FK `<text 34>`: shmo → CUSTOMERS.SHMO

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `shmo` | int(4) | بله | — |  |
| `date_` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `total` | money(8) | خیر | — |  |
| `sharh` | text(16) | خیر | — |  |
| `tax` | money(8) | بله | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `Avarez` | money(8) | بله | — |  |
| `VisRDF` | int(4) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `UserID` | int(4) | بله | — |  |
| `IsOtherFiscalYears` | bit(1) | خیر | — | ((0)) |

### `b_az_mosh_temp` — 0 ردیف (تخمین)
- کلیدها: PK_b_az_mosh_temp(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | — |  |
| `naka` | varchar(500) | بله | — |  |
| `shka` | bigint(8) | بله | — |  |
| `rdf_anbar` | int(4) | بله | — |  |
| `tedj` | int(4) | بله | — |  |
| `tedv` | decimal(9) | بله | — |  |
| `tedb` | decimal(9) | بله | — |  |
| `mohvah` | int(4) | بله | — |  |
| `date__` | char(10) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `kasr_ez` | int(4) | بله | — |  |
| `dis` | varchar(300) | بله | — |  |
| `mabj` | money(8) | بله | — |  |
| `invepgh` | money(8) | بله | — |  |
| `na_anb` | varchar(300) | بله | — |  |
| `time_` | int(4) | بله | — |  |
| `shsanad` | int(4) | بله | — |  |
| `shmo` | bigint(8) | بله | — |  |
| `vis_per` | decimal(9) | بله | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `Avarez` | decimal(9) | بله | — |  |
| `AvarezValue` | money(8) | بله | — |  |
| `VisRdf` | int(4) | بله | — |  |
| `PerTaf` | decimal(9) | خیر | — | ((0)) |
| `Litakhma` | money(8) | خیر | — | ((0)) |
| `UserID` | int(4) | بله | — |  |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `PerPromotion` | decimal(9) | بله | — |  |
| `PriceFinished` | decimal(9) | بله | — |  |
| `back_sanad` | int(4) | بله | — |  |
| `VarietyId` | int(4) | خیر | — |  |

### `back_sanad` — 77 ردیف (تخمین)
- کلیدها: PK_back_sanad(PK)=shomare, kind
- FK `FK_back_sanad_sys_users`: TaeedUserId → sys_users.user_id
- FK `FK_back_sanad_sys_users1`: TaeedWarehousUserId → sys_users.user_id
- FK `FK_back_sanad_sys_users2`: UserID → sys_users.user_id
- FK `FK_back_sanad_sys_users3`: DeleteUser → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shomare` | int(4) | خیر | — |  |
| `kind` | int(4) | خیر | — |  |
| `shfac` | int(4) | خیر | — |  |
| `date_` | char(10) | خیر | — |  |
| `mab` | money(8) | خیر | — |  |
| `moname` | nvarchar(1000) | بله | — |  |
| `tozih` | nvarchar(1000) | بله | — |  |
| `Tax` | money(8) | بله | — |  |
| `Avarez` | money(8) | بله | — |  |
| `VisSahm` | money(8) | بله | — |  |
| `Promotion` | money(8) | بله | — |  |
| `Perfine` | decimal(9) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `VisIDPerfine` | int(4) | بله | — |  |
| `RevocationID` | int(4) | بله | — | ((0)) |
| `UserID` | int(4) | بله | — |  |
| `Taeed` | bit(1) | خیر | — | ((0)) |
| `TaeedDate` | char(10) | بله | — |  |
| `TaeedUserId` | int(4) | بله | — |  |
| `TaeedWarehous` | bit(1) | خیر | — | ((0)) |
| `TaeedWarehousUserId` | int(4) | بله | — |  |
| `TaeedWarehousDate` | char(10) | بله | — |  |
| `Hour` | char(8) | بله | — |  |
| `UIdTax` | nvarchar(200) | بله | — |  |
| `SubmittedTax` | int(4) | خیر | — | ((0)) |
| `MsgErrorTax` | nvarchar(-1) | بله | — |  |
| `DateSendToSystemTax` | nchar(20) | بله | — |  |
| `TImeSendToSystemTax` | nchar(20) | بله | — |  |
| `TaxUniqueID` | nvarchar(100) | بله | — |  |
| `SubmitTaxText` | nvarchar(1000) | بله | — |  |
| `TaxUniqueIDReference` | nvarchar(100) | بله | — |  |
| `DescriptionDelete` | nvarchar(-1) | بله | — |  |
| `InvoiceSerialTax` | nvarchar(100) | بله | — |  |
| `TaxRefrenceNumber` | nvarchar(1000) | بله | — |  |
| `TaxManualDate` | nvarchar(20) | بله | — |  |
| `FiscalId` | nvarchar(100) | بله | — |  |
| `SalMaliName` | nvarchar(1000) | بله | — |  |
| `Shmo` | bigint(8) | بله | — |  |
| `VisitorId` | int(4) | بله | — |  |
| `isSync` | bit(1) | خیر | — | ((0)) |
| `UniqueID` | nvarchar(100) | بله | — |  |
| `DoneDate` | nvarchar(20) | بله | — |  |
| `DeleteDate` | nvarchar(20) | بله | — |  |
| `DeleteUser` | int(4) | بله | — |  |

### `back_sanad_history` — 2 ردیف (تخمین)
- کلیدها: PK_back_sanad_history(PK)=shomare, kind, Rdf__
- FK `<text 37>`: kind → back_sanad_kind.id
- FK `<text 31>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shomare` | int(4) | خیر | — |  |
| `kind` | int(4) | خیر | — |  |
| `Rdf__` | int(4) | خیر | — |  |
| `shfac` | int(4) | خیر | — |  |
| `date_` | char(10) | خیر | — |  |
| `mab` | money(8) | خیر | — |  |
| `moname` | nvarchar(1000) | بله | — |  |
| `tozih` | nvarchar(1000) | بله | — |  |
| `Tax` | money(8) | بله | — |  |
| `Avarez` | money(8) | بله | — |  |
| `VisSahm` | money(8) | بله | — |  |
| `Promotion` | money(8) | بله | — |  |
| `Perfine` | decimal(9) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `VisIDPerfine` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `RevocationID` | int(4) | بله | — | ((0)) |
| `Shmo` | bigint(8) | بله | — |  |
| `VisitorId` | int(4) | بله | — |  |
| `DoneDate` | nvarchar(20) | بله | — |  |

### `back_sanad_kind` — 9 ردیف (تخمین)
- کلیدها: PK_back_sanad_kind(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | — |  |
| `name` | nvarchar(100) | خیر | — |  |

### `backbuy_temp` — 0 ردیف (تخمین)
- کلیدها: PK_backbuy_temp(PK)=shfackh, rdf, shka, rdf__

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfackh` | int(4) | خیر | — |  |
| `rdf` | int(4) | خیر | — |  |
| `shka` | int(4) | خیر | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `mohvah` | int(4) | خیر | — |  |
| `date__` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `pertafif` | decimal(9) | بله | — |  |
| `sh_back_sanad` | int(4) | خیر | — |  |
| `ptax` | money(8) | خیر | — |  |
| `ppromotion` | decimal(9) | خیر | — |  |
| `pavarez` | decimal(9) | خیر | — |  |
| `sysid` | int(4) | خیر | — | ((1)) |
| `JozPrice` | money(8) | خیر | — |  |
| `TafifHajmi1Enterd` | decimal(9) | خیر | — | ((0)) |
| `TedVahEnterd` | decimal(9) | خیر | — |  |
| `TedJozEnterd` | int(4) | خیر | — |  |
| `TafifHajmi2Enterd` | decimal(9) | خیر | — | ((0)) |
| `TedBasteBandiEnterd` | decimal(9) | خیر | — |  |
| `RdfAnbarEnterd` | int(4) | خیر | — |  |
| `Shmo` | int(4) | خیر | — |  |
| `TedVahFel` | decimal(9) | خیر | — |  |
| `TedJozFel` | int(4) | خیر | — |  |
| `UserID` | int(4) | بله | — |  |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `VarietyId` | int(4) | خیر | — |  |

### `backsail_pish` — 0 ردیف (تخمین)
- کلیدها: PK_backsail_pish(PK)=RowId
- FK `FK_backsail_pish_sys_users`: UserIdConfirm → sys_users.user_id
- FK `FK_backsail_pish_sys_users1`: UserIdEbtal → sys_users.user_id
- FK `FK_backsail_pish_taraz`: ShTaraz → taraz.sh_taraz
- FK `FK_backsail_pish_visitors`: VisRdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | bigint(8) | خیر | بله |  |
| `ShTaraz` | int(4) | بله | — |  |
| `Shfacfo` | bigint(8) | خیر | — |  |
| `VisRdf` | int(4) | بله | — |  |
| `Lat` | float(8) | خیر | — |  |
| `Lng` | float(8) | خیر | — |  |
| `Date` | char(10) | خیر | — |  |
| `Time` | char(8) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `IsBackSail` | bit(1) | خیر | — |  |
| `FlagConvert` | tinyint(1) | خیر | — | ((0)) |
| `shSanad` | bigint(8) | خیر | — | ((0)) |
| `IsConfirmEbtal` | smallint(2) | خیر | — | ((0)) |
| `DateConfirm` | char(10) | بله | — |  |
| `UserIdConfirm` | int(4) | بله | — |  |
| `DateEbtal` | char(10) | بله | — |  |
| `UserIdEbtal` | int(4) | بله | — |  |

### `backsail_temp` — 0 ردیف (تخمین)
- کلیدها: PK_backsail_temp(PK)=shfacfo, rdf__, shka, rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfacfo` | int(4) | خیر | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `shka` | int(4) | خیر | — |  |
| `tedvah` | decimal(9) | بله | — |  |
| `tedjoz` | int(4) | بله | — |  |
| `tedbastebandi` | decimal(9) | بله | — |  |
| `bastebandi` | nvarchar(600) | بله | — |  |
| `vahsanj` | nvarchar(600) | بله | — |  |
| `mabjoz` | money(8) | بله | — |  |
| `edit` | char(1) | بله | — |  |
| `pertafif` | decimal(9) | بله | — |  |
| `pervis` | decimal(9) | بله | — |  |
| `visrdf` | int(4) | بله | — |  |
| `mohvah` | int(4) | بله | — |  |
| `rdf` | int(4) | خیر | — |  |
| `tedvah_fo` | decimal(9) | بله | — |  |
| `tedjoz_fo` | int(4) | بله | — |  |
| `tedbastebandi_fo` | decimal(9) | بله | — |  |
| `vahprice` | money(8) | بله | — |  |
| `jozprice` | money(8) | بله | — |  |
| `linesum_fel` | money(8) | بله | — |  |
| `litakhma_fel` | money(8) | بله | — |  |
| `naka` | nvarchar(1000) | بله | — |  |
| `invepgh` | money(8) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `date` | char(10) | بله | — |  |
| `mod` | int(4) | بله | — |  |
| `vis_rdf` | int(4) | بله | — |  |
| `sh_back_sanad` | int(4) | بله | — |  |
| `anb_m` | int(4) | بله | — |  |
| `n_anb_m` | nvarchar(500) | بله | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tax_fel` | money(8) | بله | — |  |
| `perFineKolMar` | decimal(9) | بله | — |  |
| `kolMar` | int(4) | بله | — |  |
| `pavarez` | decimal(9) | بله | — |  |
| `avarez_fel` | money(8) | بله | — |  |
| `sysid` | int(4) | بله | — |  |
| `TafifHajmi1` | decimal(9) | بله | — |  |
| `PromotionValue` | decimal(9) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `PriceFinished` | money(8) | بله | — |  |
| `TamamJozSanavat` | money(8) | بله | — |  |
| `VarietyId` | int(4) | خیر | — |  |

### `ban_act` — 1232 ردیف (تخمین)
- کلیدها: PK_ban_act(PK)=rdf
- FK `FK_ban_act_BANK`: bank_rdf → BANK.RDF
- FK `FK_ban_act_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `bank_rdf` | int(4) | خیر | — |  |
| `act_id` | int(4) | بله | — |  |
| `act_dis` | varchar(500) | بله | — |  |
| `act_bes` | money(8) | خیر | — | ((0)) |
| `act_bed` | money(8) | خیر | — | ((0)) |
| `act_date` | char(10) | خیر | — |  |
| `done_act` | char(10) | بله | — |  |
| `DocNumber` | int(4) | بله | — |  |
| `Ghno` | int(4) | بله | — | ((0)) |
| `isActive` | bit(1) | بله | — |  |
| `ShowInReport` | bit(1) | خیر | — | ((1)) |
| `Time` | nvarchar(40) | بله | — |  |
| `AccDocNumber` | int(4) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `isEdited` | bit(1) | خیر | — | ((0)) |

### `baze` — 2 ردیف (تخمین)
- کلیدها: PK_baze(PK)=rdf
- FK `FK_baze_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `name` | varchar(50) | خیر | — |  |
| `sta` | char(10) | خیر | — |  |
| `end_` | char(10) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `CopyFrom` | int(4) | خیر | — | ((0)) |
| `UserID` | int(4) | خیر | — |  |

### `buyfact` — 62 ردیف (تخمین)
- کلیدها: PK_buyfact(PK)=shfackh, rdf__
- FK `FK_buyfact_CUSTOMERS`: shmo → CUSTOMERS.SHMO
- FK `FK_buyfact_sys_users`: UserID → sys_users.user_id
- FK `FK_buyfact_sys_users1`: TaeedUserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfackh` | bigint(8) | خیر | — |  |
| `shmo` | int(4) | خیر | — |  |
| `DATE` | char(10) | خیر | — |  |
| `paskeraye` | money(8) | بله | — |  |
| `barbari` | money(8) | بله | — |  |
| `sumlineall` | money(8) | خیر | — |  |
| `all` | money(8) | خیر | — |  |
| `tafif` | money(8) | بله | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `tafif_agh` | money(8) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `shbarname` | varchar(30) | خیر | — |  |
| `tax` | money(8) | بله | — |  |
| `taeed` | int(4) | بله | — |  |
| `takh_b` | money(8) | بله | — |  |
| `paskeraye_az_mosh` | int(4) | بله | — |  |
| `promotion` | money(8) | بله | — | ((0)) |
| `CashType` | int(4) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — | ((1)) |
| `SumTafifHajmi2` | decimal(9) | خیر | — | ((0)) |
| `Avarez` | decimal(9) | بله | — |  |
| `Explain` | nvarchar(1000) | بله | — |  |
| `SumLineAllFel` | decimal(9) | خیر | — | ((0)) |
| `TafifAghlamFel` | decimal(9) | خیر | — | ((0)) |
| `PromotionFel` | decimal(9) | خیر | — | ((0)) |
| `TafifHajmi1Fel` | decimal(9) | خیر | — | ((0)) |
| `TaxFel` | decimal(9) | خیر | — | ((0)) |
| `AvarezFel` | decimal(9) | خیر | — | ((0)) |
| `TafifHajmi2Fel` | decimal(9) | خیر | — | ((0)) |
| `AllFel` | decimal(9) | خیر | — | ((0)) |
| `DateModatPardakht` | nvarchar(20) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `TaeedDate` | char(10) | بله | — |  |
| `TaeedUserId` | int(4) | بله | — |  |
| `Hour` | char(8) | بله | — |  |
| `DescriptionDelete` | nvarchar(-1) | بله | — |  |
| `Tasviyeh` | bit(1) | خیر | — | ((0)) |
| `MablaghPardakht` | money(8) | خیر | — | ((0)) |
| `MoeinIdKerayehHaml` | bigint(8) | بله | — |  |
| `TafsilIDKerayehHaml` | bigint(8) | بله | — |  |
| `MoeinIdTakhliyeBar` | bigint(8) | بله | — |  |
| `TafsilIdTakhliyeBar` | bigint(8) | بله | — |  |
| `IsRegisteredInSystem` | bit(1) | خیر | — | ((0)) |
| `TaxUniqueID` | nvarchar(-1) | بله | — |  |
| `SystemRegistrationDate` | nvarchar(-1) | بله | — |  |
| `SystemSettlementDate` | nvarchar(-1) | بله | — |  |

### `buyfact_pish` — 0 ردیف (تخمین)
- کلیدها: PK_buyfact_pish(PK)=shfackh, rdf__
- FK `FK_buyfact_pish_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfackh` | bigint(8) | خیر | — |  |
| `shmo` | int(4) | خیر | — |  |
| `DATE` | char(10) | خیر | — |  |
| `shfacthand` | varchar(500) | بله | — |  |
| `shahrdari` | money(8) | بله | — |  |
| `paskeraye` | money(8) | بله | — |  |
| `barbari` | money(8) | بله | — |  |
| `sumlineall` | money(8) | خیر | — |  |
| `jamkol` | money(8) | خیر | — |  |
| `all` | money(8) | خیر | — |  |
| `tafif` | money(8) | بله | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `tafif_agh` | money(8) | خیر | — |  |
| `isret` | char(1) | خیر | — |  |
| `ismodify` | char(1) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `shbarname` | varchar(30) | خیر | — |  |
| `tax` | money(8) | بله | — |  |
| `taeed` | int(4) | بله | — |  |
| `takh_b` | money(8) | بله | — |  |
| `name_` | varchar(50) | بله | — | ('--') |
| `paskeraye_az_mosh` | int(4) | بله | — |  |
| `promotion` | money(8) | بله | — | ((0)) |
| `FactorShode` | bigint(8) | بله | — |  |
| `CustGroupRdf` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |

### `cars` — 3 ردیف (تخمین)
- کلیدها: PK_cars(PK)=rdf_car
- FK `FK_cars_Device`: DeviceID → Device.DeviceID
- FK `FK_cars_visitors`: rdf_vis → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_car` | int(4) | خیر | بله |  |
| `name` | varchar(30) | بله | — |  |
| `color` | varchar(20) | بله | — |  |
| `sh_pelak` | varchar(30) | بله | — |  |
| `vazn` | float(8) | بله | — |  |
| `vazn_bar` | float(8) | بله | — |  |
| `rdf_vis` | int(4) | بله | — |  |
| `DeviceID` | int(4) | بله | — |  |

### `chkamani` — 0 ردیف (تخمین)
- کلیدها: PK_chkamani(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `date_` | char(10) | بله | — |  |
| `sardate` | char(10) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `shhe` | varchar(500) | خیر | — |  |
| `p` | int(4) | خیر | — |  |
| `mab` | money(8) | بله | — |  |
| `taraf` | varchar(500) | بله | — |  |
| `shobe` | varchar(500) | بله | — |  |
| `status` | int(4) | خیر | — |  |
| `shahrestan` | int(4) | خیر | — |  |
| `shchk` | varchar(30) | بله | — |  |
| `Expalin` | nvarchar(1000) | بله | — |  |
| `BankID` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `DateBargashti` | nvarchar(20) | بله | — |  |
| `UserIDBargashti` | int(4) | بله | — |  |
| `Shmo` | int(4) | بله | — |  |
| `Rdf_` | int(4) | بله | — |  |
| `AccountOwner` | nvarchar(200) | بله | — |  |

### `chkamaniHistory` — 0 ردیف (تخمین)
- کلیدها: PK_chkamaniHistory_1(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RdfCheckAmani` | int(4) | خیر | — |  |
| `date_` | char(10) | بله | — |  |
| `sardate` | char(10) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `shhe` | varchar(500) | خیر | — |  |
| `p` | int(4) | خیر | — |  |
| `mab` | money(8) | بله | — |  |
| `taraf` | varchar(500) | بله | — |  |
| `shobe` | varchar(500) | بله | — |  |
| `status` | int(4) | خیر | — |  |
| `shahrestan` | int(4) | خیر | — |  |
| `shchk` | varchar(30) | بله | — |  |
| `Expalin` | nvarchar(1000) | بله | — |  |
| `BankID` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `Shmo` | int(4) | بله | — |  |
| `Rdf_` | int(4) | بله | — |  |
| `RowID` | int(4) | خیر | بله |  |

### `chkbatch` — 15 ردیف (تخمین)
- کلیدها: PK_chkbatch(PK)=rdf_bank, chkbatch_num
- FK `FK_chkbatch_BANK`: rdf_bank → BANK.RDF
- FK `FK_chkbatch_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `rdf_bank` | int(4) | خیر | — |  |
| `tedbarg` | int(4) | خیر | — |  |
| `st_serial` | bigint(8) | خیر | — |  |
| `chkbatch_num` | int(4) | خیر | — |  |
| `type` | int(4) | بله | — |  |
| `BANKRDF` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |

### `chks_temp` — 0 ردیف (تخمین)
- کلیدها: PK_chks_temp(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | bigint(8) | خیر | — |  |
| `sardate` | char(10) | بله | — |  |
| `shgetchk` | varchar(40) | بله | — |  |
| `getchkmab` | money(8) | بله | — |  |
| `act` | int(4) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `date__` | char(10) | بله | — |  |
| `rdf_shhes` | int(4) | بله | — |  |
| `getchkshhes` | varchar(30) | بله | — |  |
| `shmo` | bigint(8) | بله | — |  |
| `moname` | varchar(500) | بله | — |  |
| `p` | int(4) | بله | — | ((0)) |
| `ghno` | int(4) | بله | — | ((0)) |
| `sysid` | int(4) | بله | — | ((1)) |
| `GhnoPardakht` | int(4) | خیر | — | ((0)) |
| `GhnoKharj` | int(4) | بله | — |  |
| `Rdf_` | int(4) | بله | — |  |
| `TafsilID` | bigint(8) | بله | — |  |
| `KarMozd` | decimal(9) | بله | — |  |
| `Explain` | nvarchar(1000) | خیر | — |  |
| `GetStatus` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |
| `Description` | nvarchar(2000) | بله | — |  |
| `Kharj_Shmo_MoeinId` | bigint(8) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — | ((0)) |

### `companyTask` — 1 ردیف (تخمین)
- کلیدها: PK_CompanyTask(PK)=CompanyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `CompanyID` | int(4) | خیر | بله |  |
| `CompanyName` | nvarchar(400) | بله | — |  |
| `Tell` | nvarchar(40) | بله | — |  |
| `Address` | nvarchar(-1) | بله | — |  |
| `ParentCompnayID` | int(4) | بله | — |  |

### `convention` — 0 ردیف (تخمین)
- کلیدها: PK_convention(PK)=conventionID
- FK `<text 32>`: conventionDurationID → conventionDuration.conventionDurationID
- FK `FK_convention_conventionType`: conventionTypeID → conventionType.conventionTypeID
- FK `FK_convention_CUSTOMERS`: SHMO → CUSTOMERS.SHMO
- FK `FK_convention_product`: productID → product.productID
- FK `FK_convention_user`: UserID → user.UserID
- FK `FK_convention_user1`: regUserID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `conventionID` | int(4) | خیر | بله |  |
| `SHMO` | int(4) | بله | — |  |
| `registerDate` | nvarchar(20) | بله | — |  |
| `startDate` | nvarchar(20) | بله | — |  |
| `conventionTypeID` | int(4) | بله | — |  |
| `productID` | int(4) | بله | — |  |
| `conventionDurationID` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |
| `regUserID` | int(4) | بله | — |  |
| `companyID` | int(4) | بله | — |  |
| `endDate` | nvarchar(20) | بله | — |  |
| `countOfSystem` | int(4) | بله | — |  |
| `Price` | decimal(9) | بله | — |  |
| `ConventionInvoiceID` | int(4) | بله | — |  |

### `conventionDuration` — 3 ردیف (تخمین)
- کلیدها: PK_conventionDuration(PK)=conventionDurationID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `conventionDurationID` | int(4) | خیر | بله |  |
| `name` | nvarchar(400) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |
| `day` | int(4) | خیر | — | ((0)) |

### `conventionType` — 2 ردیف (تخمین)
- کلیدها: PK_conventionType(PK)=conventionTypeID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `conventionTypeID` | int(4) | خیر | بله |  |
| `name` | nvarchar(400) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |
| `conventionBasic` | bit(1) | خیر | — | ((0)) |

### `cus_image` — 2724 ردیف (تخمین)
- کلیدها: PK_cus_image(PK)=RDF
- FK `FK_cus_image_CUSTOMERS`: shmo → CUSTOMERS.SHMO

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shmo` | int(4) | خیر | — |  |
| `image` | image(16) | بله | — |  |
| `RDF` | int(4) | خیر | بله |  |

### `cust_act` — 6176 ردیف (تخمین)
- کلیدها: PK_cust_act(PK)=rdf_, shmo
- FK `FK_cust_act_CUSTOMERS`: shmo → CUSTOMERS.SHMO
- FK `FK_cust_act_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_` | int(4) | خیر | بله |  |
| `shmo` | int(4) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `act_bes` | money(8) | خیر | — | ((0)) |
| `act_bed` | money(8) | خیر | — | ((0)) |
| `act_dis` | nvarchar(1000) | خیر | — | ('بابت') |
| `act_id` | int(4) | خیر | — |  |
| `ghno` | bigint(8) | خیر | — | ((0)) |
| `done_date` | char(10) | بله | — |  |
| `t_time` | datetime(8) | بله | — |  |
| `ShowInReport` | bit(1) | خیر | — | ((1)) |
| `DocNumber` | int(4) | بله | — |  |
| `MoghayeratDiscription` | nvarchar(3000) | بله | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `isActive` | bit(1) | بله | — | ((1)) |
| `fk_id` | bigint(8) | بله | — |  |
| `Rdf_ForDar` | int(4) | بله | — |  |
| `GhestPriceTemp` | decimal(9) | بله | — |  |
| `AccDocNumber` | int(4) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `isEdited` | bit(1) | خیر | — | ((0)) |

### `custgroup` — 11 ردیف (تخمین)
- کلیدها: PK_custgroup(PK)=group_rdf
- FK `FK_custgroup_custgroup`: group_rdf → custgroup.group_rdf
- FK `FK_custgroup_GroupTafsil`: TafsilGpId → GroupTafsil.GroupID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `group_name` | varchar(30) | خیر | — |  |
| `group_rdf` | int(4) | خیر | بله |  |
| `stdate` | char(10) | بله | — |  |
| `price` | int(4) | بله | — |  |
| `ted_rooz` | int(4) | بله | — |  |
| `AccType` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `PerGain` | decimal(9) | بله | — |  |
| `TafsilGpId` | bigint(8) | بله | — |  |
| `buyAccountingMoeinID` | bigint(8) | بله | — |  |
| `saleAccountingMoeinID` | bigint(8) | بله | — |  |

### `customerComputerLog` — 0 ردیف (تخمین)
- کلیدها: PK_customerComputerLog(PK)=customerComputerLogID
- FK `<text 33>`: sellConventionID → convention.conventionID
- FK `<text 34>`: supportConventionID → convention.conventionID
- FK `<text 40>`: customerComputerID → customerComputers.customerComputerID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `customerComputerLogID` | int(4) | خیر | بله |  |
| `customerComputerID` | int(4) | بله | — |  |
| `sellConventionID` | int(4) | بله | — |  |
| `supportConventionID` | int(4) | بله | — |  |

### `customerComputers` — 0 ردیف (تخمین)
- کلیدها: <text 29>(PK)=customerComputerID
- FK `<text 30>`: CustomerSHMO → CUSTOMERS.SHMO
- FK `<text 37>`: operatingSystemID → operatingSystem.operatingSystemID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `customerComputerID` | int(4) | خیر | بله |  |
| `userFullName` | nvarchar(200) | بله | — |  |
| `operatingSystemID` | int(4) | بله | — |  |
| `macAddress` | nvarchar(200) | بله | — |  |
| `IPValid` | nvarchar(200) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |
| `CustomerSHMO` | int(4) | بله | — |  |
| `ActiveStatus` | bit(1) | بله | — |  |
| `NameActiveStatus` | nvarchar(100) | بله | — |  |
| `IsServer` | bit(1) | بله | — |  |

### `customer_news` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id
- FK `<text 31>`: shmo → CUSTOMERS.SHMO
- FK `customer_news_news_id_fk`: news_id → news.id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `shmo` | int(4) | بله | — |  |
| `news_id` | int(4) | بله | — |  |

### `dar` — 1063 ردیف (تخمین)
- کلیدها: pk_dar(PK)=ghno, p, Rdf_
- FK `FK_dar_darDescriptionType`: darDescriptionTypeID → darDescriptionType.rowId
- FK `FK_dar_DocumentSourceKind`: DocumentSourceID → DocumentSourceKind.ID
- FK `FK_dar_PeygiriMotalebat`: PeygiriMotalebatID → PeygiriMotalebat.ID
- FK `FK_dar_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ghno` | int(4) | خیر | — |  |
| `naghd` | money(8) | خیر | — | ((0)) |
| `ted_chk` | int(4) | خیر | — | ((0)) |
| `p` | int(4) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `d_p_dis` | varchar(500) | خیر | — |  |
| `shmo` | int(4) | خیر | — |  |
| `mab` | money(8) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `shfac` | int(4) | بله | — |  |
| `rdf_vis` | int(4) | بله | — |  |
| `rdf_mamorp` | int(4) | بله | — |  |
| `rdf_mamorm` | int(4) | بله | — |  |
| `rdf_driver` | int(4) | بله | — |  |
| `mabcheck` | money(8) | بله | — | ((0)) |
| `tafif` | money(8) | بله | — | ((0)) |
| `sysid` | int(4) | بله | — | ((1)) |
| `TafsilCode` | nchar(26) | بله | — |  |
| `ClockTime` | nvarchar(200) | بله | — |  |
| `TakhfifHazineh` | money(8) | بله | — | ((0)) |
| `TakhfifDaramad` | money(8) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `Rdf_` | int(4) | خیر | — | ((1)) |
| `OtherPriceForFactor` | decimal(9) | خیر | — | ((0)) |
| `EbtalDis` | nvarchar(1000) | بله | — |  |
| `IsFinal` | bit(1) | خیر | — | ((1)) |
| `TaeedUserID` | int(4) | بله | — |  |
| `TaeedDateServer` | nvarchar(20) | بله | — |  |
| `TaeedTimeServer` | nvarchar(100) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `PeygiriMotalebatID` | int(4) | بله | — |  |
| `darDescriptionTypeID` | bigint(8) | بله | — |  |
| `UniqueID` | nvarchar(100) | بله | — |  |
| `isSync` | bit(1) | خیر | — | ((0)) |
| `DocumentSourceID` | int(4) | خیر | — | ((1)) |

### `darDescriptionType` — 3 ردیف (تخمین)
- کلیدها: PK_darDescriptionType(PK)=rowId
- FK `FK_darDescriptionType_Moein`: moeinID → Moein.MoeinID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rowId` | bigint(8) | خیر | بله |  |
| `Description` | nvarchar(400) | خیر | — |  |
| `p` | int(4) | خیر | — |  |
| `moeinID` | bigint(8) | بله | — |  |
| `TransferCode` | nvarchar(100) | بله | — | (NULL) |

### `department` — 0 ردیف (تخمین)
- کلیدها: PK_Department(PK)=DepartmentID
- FK `FK_Department_Department`: CompanyID → companyTask.CompanyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `DepartmentID` | int(4) | خیر | بله |  |
| `DepartmentName` | nvarchar(100) | بله | — |  |
| `SubDepartmentID` | int(4) | بله | — |  |
| `CompanyID` | int(4) | بله | — |  |

### `deviceAnbar` — 0 ردیف (تخمین)
- کلیدها: PK_deviceAnbar(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `ActivationCode` | nvarchar(100) | خیر | — |  |
| `CupId` | nvarchar(100) | خیر | — |  |
| `Status` | int(4) | خیر | — |  |
| `ActivationDate` | char(10) | خیر | — |  |
| `ExpirationDate` | char(10) | خیر | — |  |
| `SysId` | int(4) | خیر | — |  |
| `DeviceType` | int(4) | خیر | — |  |
| `DeviceName` | nvarchar(100) | خیر | — |  |
| `AppId` | nvarchar(1000) | خیر | — |  |

### `dtproperties` — 14 ردیف (تخمین)
- کلیدها: pk_dtproperties(PK)=id, property

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `objectid` | int(4) | بله | — |  |
| `property` | varchar(64) | خیر | — |  |
| `value` | varchar(255) | بله | — |  |
| `uvalue` | nvarchar(510) | بله | — |  |
| `lvalue` | image(16) | بله | — |  |
| `version` | int(4) | خیر | — | (0) |

### `forosh_price` — 1803 ردیف (تخمین)
- کلیدها: PK_forosh_price(PK)=VarietyID
- FK `FK_forosh_price_inventory`: shka → inventory.shka
- FK `FK_forosh_price_Variety`: VarietyID → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka` | bigint(8) | خیر | — | (0) |
| `forosh1` | money(8) | خیر | — | ((0)) |
| `mp1` | int(4) | خیر | — | ((0)) |
| `pv1` | decimal(9) | خیر | — | ((0)) |
| `forosh2` | money(8) | خیر | — | ((0)) |
| `mp2` | int(4) | خیر | — | ((0)) |
| `pv2` | decimal(9) | خیر | — | ((0)) |
| `forosh3` | money(8) | خیر | — | ((0)) |
| `mp3` | int(4) | خیر | — | ((0)) |
| `pv3` | decimal(9) | خیر | — | ((0)) |
| `forosh4` | money(8) | خیر | — | ((0)) |
| `mp4` | int(4) | خیر | — | ((0)) |
| `pv4` | decimal(9) | خیر | — | ((0)) |
| `forosh5` | money(8) | خیر | — | ((0)) |
| `mp5` | int(4) | خیر | — | ((0)) |
| `pv5` | decimal(9) | خیر | — | ((0)) |
| `naka` | nvarchar(1000) | بله | — |  |
| `group_rdf` | int(4) | بله | — |  |
| `active` | char(1) | بله | — |  |
| `MinPrice` | money(8) | خیر | — | ((0)) |
| `MaxPrice` | money(8) | خیر | — | ((0)) |
| `DiscountForosh1` | decimal(9) | بله | — |  |
| `DiscountForosh2` | decimal(9) | بله | — |  |
| `DiscountForosh3` | decimal(9) | بله | — |  |
| `DiscountForosh4` | decimal(9) | بله | — |  |
| `DiscountForosh5` | decimal(9) | بله | — |  |
| `VarietyID` | int(4) | خیر | — |  |

### `getchk` — 118 ردیف (تخمین)
- کلیدها: PK_getchk(PK)=rdf
- FK `FK_getchk_CheckTypes`: CheckTypeID → CheckTypes.ID
- FK `FK_getchk_Moein`: MoeinId → Moein.MoeinID
- FK `FK_getchk_sys_users`: UserID → sys_users.user_id
- FK `FK_getchk_Tafsil`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | bigint(8) | خیر | بله |  |
| `getdate` | char(10) | خیر | — | ('ذکر نشده') |
| `sardate` | char(10) | خیر | — | ('ذکر نشده') |
| `getchkshhes` | nvarchar(600) | خیر | — |  |
| `getchbank` | nvarchar(600) | خیر | — | ('ذکر نشده') |
| `getchkshobe` | nvarchar(700) | خیر | — | ('ذکر نشده') |
| `shgetchk` | nvarchar(800) | خیر | — | ('ذکرنشده') |
| `getchkmab` | money(8) | خیر | — | ((0)) |
| `shmo` | bigint(8) | خیر | — | ((0)) |
| `VIRTUALNAME` | varchar(500) | خیر | — | ('ذکر نشده') |
| `getchkdis` | nvarchar(-1) | خیر | — | ('ذکر نشده') |
| `shahrestan` | int(4) | خیر | — |  |
| `chk_satus` | int(4) | خیر | — | ((0)) |
| `our_bankrdf` | int(4) | خیر | — | ((0)) |
| `our_bankrdf_date` | char(10) | خیر | — | ('ذکر نشده') |
| `naghddate` | char(10) | خیر | — | ('ذکر نشده') |
| `back` | char(1) | خیر | — | ('f') |
| `kharj_date` | char(10) | خیر | — | ('ذکر نشده') |
| `kharj_shmo` | bigint(8) | خیر | — | ((0)) |
| `karj_virtualname` | varchar(500) | بله | — | ('ذکر نشده') |
| `ghno` | int(4) | خیر | — | ((0)) |
| `rdf_in_ghno` | int(4) | خیر | — | ((0)) |
| `done_date` | char(10) | بله | — |  |
| `vagozarande` | varchar(500) | بله | — |  |
| `our_bankrdf_donedate` | char(10) | بله | — |  |
| `naghddonedate` | char(10) | بله | — |  |
| `kharj_done_date` | char(10) | بله | — |  |
| `mod` | int(4) | بله | — |  |
| `vis_rdf` | int(4) | بله | — |  |
| `kharj_mod` | int(4) | بله | — |  |
| `shfac` | int(4) | بله | — |  |
| `rdf_mamorm` | int(4) | بله | — | ((0)) |
| `rdf_mamorp` | int(4) | بله | — | ((0)) |
| `rdf_driver` | int(4) | بله | — | ((0)) |
| `soo` | int(4) | خیر | — | ((0)) |
| `sysid` | int(4) | بله | — | ((1)) |
| `DocID` | bigint(8) | بله | — |  |
| `FromAtiranDocID` | bigint(8) | بله | — |  |
| `Description` | nvarchar(1000) | بله | — |  |
| `p` | int(4) | بله | — | ((0)) |
| `GhnoPardakht` | int(4) | خیر | — | ((0)) |
| `GhnoKharj` | int(4) | بله | — |  |
| `Rdf_` | int(4) | خیر | — | ((1)) |
| `TafsilID` | bigint(8) | بله | — |  |
| `Rdf_P` | int(4) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `ShenaseSayad` | nvarchar(100) | بله | — |  |
| `RegistrationInquiry` | bit(1) | خیر | — | ((0)) |
| `IdDocumentZirSarfasl` | int(4) | بله | — |  |
| `ToNewYear` | bit(1) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — |  |
| `kharj_shmo_Moein` | bigint(8) | بله | — |  |
| `DateOfReceipt` | char(10) | بله | — |  |
| `CheckTypeID` | int(4) | خیر | — | ((1)) |

### `grouh` — 10 ردیف (تخمین)
- کلیدها: PK_grouh(PK)=GrouhID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GrouhCode` | nchar(4) | خیر | — |  |
| `Name` | nvarchar(2000) | خیر | — |  |
| `GrouhID` | bigint(8) | خیر | بله |  |
| `Daraie_Bedehi` | int(4) | خیر | — | ((0)) |
| `IsEdit` | bit(1) | خیر | — | ((0)) |

### `group_news` — 0 ردیف (تخمین)
- کلیدها: PK_group_news(PK)=id
- FK `FK_group_news_custgroup`: group_rdf → custgroup.group_rdf
- FK `group_news_news_id_fk`: news_id → news.id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `news_id` | int(4) | بله | — |  |
| `group_rdf` | int(4) | بله | — |  |

### `havaleh` — 346 ردیف (تخمین)
- کلیدها: PK_havaleh(PK)=ghno, rdf, p, Rdf_
- FK `FK_havaleh_dar`: ghno → dar.ghno
- FK `FK_havaleh_dar`: p → dar.p
- FK `FK_havaleh_dar`: Rdf_ → dar.Rdf_

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ghno` | int(4) | خیر | — |  |
| `rdf` | bigint(8) | خیر | — |  |
| `p` | int(4) | خیر | — |  |
| `shhavaleh` | varchar(250) | خیر | — | ('ذکر نشده') |
| `shmo` | bigint(8) | خیر | — |  |
| `bankname` | varchar(250) | خیر | — | ('ذکر نشده') |
| `shobe` | varchar(500) | خیر | — | ('ذکر نشده') |
| `mab` | money(8) | خیر | — |  |
| `user` | nvarchar(2000) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `babat` | varchar(500) | خیر | — |  |
| `mod` | int(4) | بله | — |  |
| `rdf_bank` | int(4) | بله | — | ((1)) |
| `Rdf_` | int(4) | خیر | — | ((1)) |

### `havalehTemp` — 0 ردیف (تخمین)
- کلیدها: PK_havalehTemp(PK)=ghno, rdf, p, Rdf_
- FK `FK_havalehTemp_dar`: ghno → dar.ghno
- FK `FK_havalehTemp_dar`: p → dar.p
- FK `FK_havalehTemp_dar`: Rdf_ → dar.Rdf_

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ghno` | int(4) | خیر | — |  |
| `rdf` | bigint(8) | خیر | — |  |
| `p` | int(4) | خیر | — |  |
| `shhavalehTemp` | varchar(250) | خیر | — | ('ذکر نشده') |
| `shmo` | bigint(8) | خیر | — |  |
| `bankname` | varchar(250) | خیر | — | ('ذکر نشده') |
| `shobe` | varchar(500) | خیر | — | ('ذکر نشده') |
| `mab` | money(8) | خیر | — |  |
| `user` | nvarchar(2000) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `babat` | varchar(500) | خیر | — |  |
| `mod` | int(4) | بله | — |  |
| `rdf_bank` | int(4) | بله | — | ((1)) |
| `Rdf_` | int(4) | خیر | — | ((1)) |

### `inventory` — 1803 ردیف (تخمین)
- کلیدها: PK_INVENTORY(PK)=shka
- FK `FK_inventory_BrandName`: BrandID → BrandName.ID
- FK `FK_inventory_InventoryType`: InventoryTypeID → InventoryType.ID
- FK `FK_inventory_kagroup`: group_rdf → kagroup.group_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka` | bigint(8) | خیر | بله |  |
| `naka` | nvarchar(1000) | خیر | — |  |
| `coka` | nvarchar(1000) | خیر | — | ('ذکر نشده') |
| `group_rdf` | int(4) | خیر | — |  |
| `vahsanj` | nvarchar(600) | خیر | — |  |
| `mohvah` | bigint(8) | خیر | — |  |
| `mojkavah` | decimal(9) | خیر | — |  |
| `mojkajoz` | int(4) | خیر | — | ((0)) |
| `reopoint` | int(4) | خیر | — |  |
| `bastebandi` | nvarchar(500) | خیر | — | ('ذکر نشده') |
| `tedbastebandi` | decimal(9) | خیر | — | ((0)) |
| `vahwe` | decimal(9) | بله | — |  |
| `vahsp` | decimal(9) | خیر | — | ((1)) |
| `visper` | decimal(9) | بله | — |  |
| `active` | char(1) | خیر | — | ('t') |
| `pure_buy_price` | money(8) | خیر | — |  |
| `buy_price` | money(8) | خیر | — |  |
| `inventory_price` | money(8) | خیر | — |  |
| `MODPAR` | int(4) | بله | — |  |
| `buyjoz` | money(8) | بله | — |  |
| `sharh` | nvarchar(4000) | بله | — |  |
| `min_sef` | int(4) | بله | — |  |
| `barbari_vahed` | money(8) | بله | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `inventory_price_tax` | money(8) | بله | — |  |
| `maxtafnaghd` | decimal(9) | بله | — |  |
| `black_list` | int(4) | بله | — | ((0)) |
| `backfine` | decimal(9) | بله | — |  |
| `ExpirationDate` | nchar(20) | بله | — |  |
| `FinalSalePrice` | money(8) | بله | — |  |
| `PAvarez` | decimal(9) | بله | — | ((0)) |
| `ImPureBuyPrice` | money(8) | خیر | — | ((0)) |
| `MaxJozForosh` | decimal(9) | خیر | — | ((0)) |
| `PerPos` | decimal(9) | بله | — |  |
| `GoodsKindID` | int(4) | بله | — |  |
| `InventoryTypeID` | int(4) | بله | — |  |
| `WithProductionSerial` | bit(1) | خیر | — | ((0)) |
| `ActiveOnlineSales` | bit(1) | خیر | — | ((1)) |
| `ActiveCapillarySales` | bit(1) | خیر | — | ((1)) |
| `siteId` | bigint(8) | بله | — |  |
| `nakaEN` | nvarchar(1000) | بله | — |  |
| `isCustomerClub` | bit(1) | بله | — |  |
| `MandatoryRoleCode` | bit(1) | بله | — | ((0)) |
| `TransferCode` | nvarchar(100) | بله | — |  |
| `mohvah2` | int(4) | خیر | — | ((1)) |
| `ProductionPrice` | money(8) | بله | — | ((0)) |
| `StuffCode` | nvarchar(200) | بله | — |  |
| `Shmo` | bigint(8) | بله | — |  |
| `BrandID` | int(4) | بله | — |  |
| `isTaxProduct` | bit(1) | خیر | — | ((1)) |
| `NtswCode` | nvarchar(100) | بله | — |  |

### `inventory_anbars` — 1803 ردیف (تخمین)
- کلیدها: PK_inventory_anbars_1(PK)=rdf_anbars, shka
- FK `FK_inventory_anbars_anbars`: rdf_anbars → anbars.rdf_anbar
- FK `<text 29>`: shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_anbars` | int(4) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `mojkavah` | decimal(9) | خیر | — |  |
| `mojkajoz` | int(4) | خیر | — |  |
| `name` | nvarchar(500) | خیر | — |  |
| `tedbastebandi` | decimal(9) | خیر | — | (0) |

### `k_tr_temp` — 0 ردیف (تخمین)
- کلیدها: PK_k_tr_temp(PK)=shka1, rdf_anb1, rdf_anb2, ProductionSeriesID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka1` | bigint(8) | خیر | — |  |
| `rdf_anb1` | int(4) | خیر | — |  |
| `tedv` | decimal(9) | خیر | — |  |
| `tedj` | int(4) | خیر | — |  |
| `tedb` | decimal(9) | خیر | — |  |
| `rdf_anb2` | int(4) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `mohvah` | int(4) | خیر | — |  |
| `na_anb1` | nvarchar(500) | خیر | — |  |
| `na_anb2` | nvarchar(500) | خیر | — |  |
| `invepgh` | money(8) | خیر | — |  |
| `naka` | nvarchar(1000) | خیر | — |  |
| `time_` | int(4) | بله | — |  |
| `sh_sanad` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `ProductionSeriesID` | bigint(8) | خیر | — | ((-1)) |
| `Rdf_` | int(4) | خیر | — | ((1)) |
| `rdf` | int(4) | خیر | — |  |
| `VarietyId` | int(4) | خیر | — |  |

### `ka_act` — 6359 ردیف (تخمین)
- کلیدها: PK_ka_act(PK)=rdf
- FK `FK_ka_act_anbars`: RdfAnbar → anbars.rdf_anbar
- FK `FK_ka_act_inventory`: shka → inventory.shka
- FK `FK_ka_act_sys_users`: UserID → sys_users.user_id
- FK `FK_ka_act_Variety`: VarietyID → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `shka` | bigint(8) | خیر | — |  |
| `act_id` | int(4) | خیر | — |  |
| `shfac` | bigint(8) | خیر | — |  |
| `rdf__` | int(4) | بله | — |  |
| `act_dis` | varchar(500) | بله | — |  |
| `act_date` | char(10) | خیر | — |  |
| `done_date` | char(10) | بله | — |  |
| `tedvah` | decimal(9) | بله | — |  |
| `tedjoz` | int(4) | بله | — |  |
| `tedbastebandi` | decimal(9) | بله | — |  |
| `active` | char(1) | بله | — |  |
| `price` | money(8) | بله | — |  |
| `invepgh` | money(8) | بله | — |  |
| `invep` | money(8) | بله | — |  |
| `gain` | money(8) | بله | — |  |
| `rdf_kh` | int(4) | بله | — |  |
| `sh_back_sanad` | int(4) | بله | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `vis_rdf` | int(4) | بله | — | ((0)) |
| `pertaf` | decimal(9) | بله | — | ((0)) |
| `pervis` | decimal(9) | بله | — | ((0)) |
| `litakhma` | money(8) | بله | — | ((0)) |
| `HAct_id` | int(4) | بله | — |  |
| `ghno` | bigint(8) | بله | — | ((1)) |
| `sysid` | int(4) | بله | — |  |
| `pavarez` | decimal(9) | بله | — | ((0)) |
| `avarez` | money(8) | بله | — | ((0)) |
| `Gift` | bit(1) | بله | — |  |
| `TafifHajmi2` | decimal(9) | بله | — |  |
| `RdfAnbar` | int(4) | خیر | — | ((0)) |
| `shmo` | int(4) | بله | — |  |
| `PerPromotion` | decimal(9) | بله | — |  |
| `TafifHajmi1` | decimal(9) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `PriceFinished` | money(8) | خیر | — | ((0)) |
| `TamamJoz` | decimal(9) | بله | — |  |
| `ShSanadAnbar` | bigint(8) | بله | — |  |
| `VarietyID` | int(4) | خیر | — |  |

### `ka_image` — 1803 ردیف (تخمین)
- کلیدها: PK_ka_image(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka` | int(4) | خیر | — |  |
| `pic` | image(16) | بله | — |  |
| `rdf` | int(4) | خیر | بله |  |

### `kagroup` — 3 ردیف (تخمین)
- کلیدها: PK_kagroup(PK)=group_rdf
- FK `FK_kagroup_kagroup`: ParentGroupRdf → kagroup.group_rdf
- FK `FK_kagroup_KalaTypeTTMS`: KalaTypeTtmsId → KalaTypeTTMS.ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `group_rdf` | int(4) | خیر | بله |  |
| `group_name` | nvarchar(1000) | خیر | — |  |
| `stdate` | datetime(8) | خیر | — |  |
| `CanNegative` | int(4) | بله | — |  |
| `ParentGroupRdf` | int(4) | بله | — |  |
| `GroupLevel` | int(4) | بله | — |  |
| `hasPic` | bit(1) | بله | — | ((0)) |
| `Active` | bit(1) | خیر | — | ((1)) |
| `siteId` | bigint(8) | بله | — |  |
| `GroupCode` | nvarchar(100) | بله | — |  |
| `KalaTypeTtmsId` | bigint(8) | بله | — |  |
| `group_code` | nvarchar(100) | بله | — |  |

### `kala_info` — 1803 ردیف (تخمین)
- کلیدها: PK_kala_info(PK)=shka
- FK `FK_kala_info_inventory`: shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka` | bigint(8) | خیر | — |  |
| `darsad_kharid` | float(8) | بله | — |  |

### `kasr_e_sanad` — 135 ردیف (تخمین)
- کلیدها: PK_kasr_e_sanad(PK)=rdf, kasr_e
- FK `FK_kasr_e_sanad_sys_users`: UserID → sys_users.user_id
- FK `FK_kasr_e_sanad_sys_users1`: TaeedUserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | — |  |
| `date_` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `total` | money(8) | خیر | — |  |
| `kasr_e` | int(4) | خیر | — |  |
| `sharh` | text(16) | خیر | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `UserID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `Taeed` | bit(1) | خیر | — | ((0)) |
| `TaeedDate` | char(10) | خیر | — | ('') |
| `TaeedUserId` | int(4) | بله | — |  |
| `Hour` | char(8) | بله | — |  |
| `ComparisonDocNumber` | int(4) | بله | — |  |
| `isSync` | bit(1) | خیر | — | ((0)) |

### `kasr_e_temp` — 0 ردیف (تخمین)
- کلیدها: PK_kasr_e_temp(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | — |  |
| `naka` | nvarchar(1000) | بله | — |  |
| `shka` | bigint(8) | بله | — |  |
| `rdf_anbar` | int(4) | بله | — |  |
| `tedj` | int(4) | بله | — |  |
| `tedv` | decimal(9) | بله | — |  |
| `tedb` | decimal(9) | بله | — |  |
| `mohvah` | int(4) | بله | — |  |
| `date__` | char(10) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `kasr_ez` | int(4) | بله | — |  |
| `dis` | nvarchar(600) | بله | — |  |
| `mabj` | money(8) | بله | — |  |
| `invepgh` | money(8) | بله | — |  |
| `na_anb` | nvarchar(600) | بله | — |  |
| `time_` | int(4) | بله | — |  |
| `shsanad` | int(4) | بله | — |  |
| `SysID` | int(4) | خیر | — | ((1)) |
| `UserID` | int(4) | بله | — |  |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `RdfSanad` | int(4) | بله | — |  |
| `VarietyId` | int(4) | خیر | — |  |

### `masir` — 4 ردیف (تخمین)
- کلیدها: PK_masir(PK)=rdf_masir
- FK `FK_masir_Quarter`: QuarterID → Quarter.ID
- FK `FK_masir_visitor`: vis_rdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_masir` | int(4) | خیر | بله |  |
| `shomare_masir` | int(4) | خیر | — |  |
| `name` | varchar(70) | خیر | — |  |
| `vis_rdf` | int(4) | بله | — |  |
| `Code` | nvarchar(1000) | خیر | — |  |
| `QuarterID` | int(4) | خیر | — |  |

### `meelano_access_roles` — 14 ردیف (تخمین)
- کلیدها: <text 30>(PK)=role_key

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `role_key` | nvarchar(120) | خیر | — |  |
| `role_label` | nvarchar(320) | بله | — |  |
| `permissions` | nvarchar(-1) | بله | — |  |
| `updated_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_access_users` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=username

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `username` | nvarchar(320) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `source` | nvarchar(160) | بله | — |  |
| `source_id` | nvarchar(200) | بله | — |  |
| `role_key` | nvarchar(120) | خیر | — | (N'user') |
| `permissions` | nvarchar(-1) | بله | — |  |
| `enabled` | bit(1) | خیر | — | ((1)) |
| `updated_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_attendance` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `event_type` | nvarchar(40) | خیر | — |  |
| `event_time` | datetime2(8) | خیر | — | (sysdatetime()) |
| `wifi_ssid` | nvarchar(400) | بله | — |  |
| `wifi_bssid` | nvarchar(200) | بله | — |  |
| `gateway` | nvarchar(160) | بله | — |  |
| `note` | nvarchar(1000) | بله | — |  |
| `lat` | float(8) | بله | — |  |
| `lng` | float(8) | بله | — |  |
| `distance_m` | float(8) | بله | — |  |
| `accuracy_m` | float(8) | بله | — |  |
| `source` | nvarchar(40) | بله | — |  |
| `zone_id` | int(4) | بله | — |  |
| `biometric` | bit(1) | بله | — |  |
| `voided` | bit(1) | خیر | — | ((0)) |
| `edited_by` | nvarchar(240) | بله | — |  |
| `edited_at` | datetime2(8) | بله | — |  |
| `manager_note` | nvarchar(1000) | بله | — |  |

### `meelano_chat_members` — 7 ردیف (تخمین)
- کلیدها: <text 30>(PK)=username

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `role` | nvarchar(60) | خیر | — | (N'user') |
| `kicked` | bit(1) | خیر | — | ((0)) |
| `muted_until` | datetime2(8) | بله | — |  |
| `last_seen` | datetime2(8) | بله | — |  |

### `meelano_chat_messages` — 6 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `sender` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `kind` | nvarchar(60) | خیر | — | (N'text') |
| `body` | nvarchar(-1) | بله | — |  |
| `attachment_name` | nvarchar(520) | بله | — |  |
| `attachment_mime` | nvarchar(320) | بله | — |  |
| `attachment_data` | varbinary(-1) | بله | — |  |
| `pinned` | bit(1) | خیر | — | ((0)) |
| `scheduled_at` | datetime2(8) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `deleted` | bit(1) | خیر | — | ((0)) |

### `meelano_chat_settings` — 7 ردیف (تخمین)
- کلیدها: <text 30>(PK)=setting_key

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `setting_key` | nvarchar(160) | خیر | — |  |
| `setting_value` | nvarchar(-1) | بله | — |  |
| `updated_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_customer_requests` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `client_uuid` | nvarchar(160) | بله | — |  |
| `visitor_username` | nvarchar(320) | بله | — |  |
| `visitor_id` | int(4) | بله | — |  |
| `visitor_name` | nvarchar(500) | بله | — |  |
| `customer_name` | nvarchar(2000) | خیر | — |  |
| `mobile` | nvarchar(120) | بله | — |  |
| `phone` | nvarchar(120) | بله | — |  |
| `address` | nvarchar(4000) | بله | — |  |
| `national_code` | nvarchar(80) | بله | — |  |
| `note` | nvarchar(4000) | بله | — |  |
| `lat` | float(8) | بله | — |  |
| `lng` | float(8) | بله | — |  |
| `status` | nvarchar(40) | خیر | — | (N'pending') |
| `created_at` | datetime(8) | خیر | — | (getdate()) |
| `decided_by` | nvarchar(500) | بله | — |  |
| `decided_at` | datetime(8) | بله | — |  |
| `decision_note` | nvarchar(2000) | بله | — |  |
| `masir_rdf` | int(4) | بله | — |  |
| `group_rdf` | int(4) | بله | — |  |
| `atiran_shmo` | int(4) | بله | — |  |
| `atiran_code` | nvarchar(200) | بله | — |  |

### `meelano_day_reports` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `visitor_username` | nvarchar(320) | بله | — |  |
| `visitor_id` | nvarchar(160) | بله | — |  |
| `report_text` | nvarchar(-1) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_delivery` — 184 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id; UQ_meelano_delivery_inv(UQ)=shfacfo, rdf__

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `shfacfo` | bigint(8) | خیر | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `inv_date` | nvarchar(20) | بله | — |  |
| `inv_time` | nvarchar(16) | بله | — |  |
| `shmo` | bigint(8) | بله | — |  |
| `customer` | nvarchar(600) | بله | — |  |
| `address` | nvarchar(1200) | بله | — |  |
| `phone` | nvarchar(160) | بله | — |  |
| `total` | decimal(9) | بله | — |  |
| `items_count` | int(4) | بله | — |  |
| `registered_by` | nvarchar(240) | بله | — |  |
| `registered_uid` | int(4) | بله | — |  |
| `vis_rdf` | int(4) | بله | — |  |
| `status` | nvarchar(40) | خیر | — | (N'open') |
| `assignee` | nvarchar(240) | بله | — |  |
| `assignee_name` | nvarchar(400) | بله | — |  |
| `claimed_at` | datetime2(8) | بله | — |  |
| `pending_to` | nvarchar(240) | بله | — |  |
| `pending_to_name` | nvarchar(400) | بله | — |  |
| `pending_at` | datetime2(8) | بله | — |  |
| `pending_note` | nvarchar(600) | بله | — |  |
| `delivered_at` | datetime2(8) | بله | — |  |
| `receiver_name` | nvarchar(400) | بله | — |  |
| `receiver_phone` | nvarchar(120) | بله | — |  |
| `signature` | varbinary(-1) | بله | — |  |
| `sign_lat` | float(8) | بله | — |  |
| `sign_lng` | float(8) | بله | — |  |
| `sign_acc` | float(8) | بله | — |  |
| `note` | nvarchar(1200) | بله | — |  |
| `close_reason` | nvarchar(600) | بله | — |  |
| `closed_by` | nvarchar(240) | بله | — |  |
| `inv_changed` | bit(1) | خیر | — | ((0)) |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `updated_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_delivery_item` — 838 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `delivery_id` | bigint(8) | خیر | — |  |
| `line_no` | int(4) | خیر | — | ((0)) |
| `shka` | bigint(8) | بله | — |  |
| `name` | nvarchar(800) | بله | — |  |
| `qty` | decimal(9) | بله | — |  |
| `cartons` | decimal(9) | بله | — |  |
| `pieces` | decimal(9) | بله | — |  |
| `per_carton` | int(4) | بله | — |  |
| `unit` | nvarchar(120) | بله | — |  |
| `amount` | decimal(9) | بله | — |  |
| `state` | nvarchar(24) | خیر | — | (N'pending') |
| `reason` | nvarchar(600) | بله | — |  |
| `changed_by` | nvarchar(240) | بله | — |  |
| `changed_at` | datetime2(8) | بله | — |  |

### `meelano_delivery_log` — 12 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `delivery_id` | bigint(8) | خیر | — |  |
| `action` | nvarchar(60) | خیر | — |  |
| `actor` | nvarchar(240) | بله | — |  |
| `actor_name` | nvarchar(400) | بله | — |  |
| `target` | nvarchar(240) | بله | — |  |
| `note` | nvarchar(1000) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_hr_advance` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `amount` | bigint(8) | خیر | — |  |
| `reason` | nvarchar(400) | بله | — |  |
| `details` | nvarchar(2000) | بله | — |  |
| `jdate` | nvarchar(20) | بله | — |  |
| `status` | nvarchar(40) | خیر | — | (N'pending') |
| `manager_note` | nvarchar(1000) | بله | — |  |
| `decided_by` | nvarchar(240) | بله | — |  |
| `decided_at` | datetime2(8) | بله | — |  |
| `paid_amount` | bigint(8) | بله | — |  |
| `paid_ref` | nvarchar(400) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_hr_holiday` — 26 ردیف (تخمین)
- کلیدها: <text 30>(PK)=jdate

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `jdate` | nvarchar(20) | خیر | — |  |
| `title` | nvarchar(400) | بله | — |  |

### `meelano_hr_inbox` — 5 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `kind` | nvarchar(60) | خیر | — |  |
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `ref_id` | bigint(8) | بله | — |  |
| `title` | nvarchar(600) | خیر | — |  |
| `body` | nvarchar(2000) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `seen_at` | datetime2(8) | بله | — |  |
| `seen_by` | nvarchar(240) | بله | — |  |

### `meelano_hr_incomplete` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id; UQ_meelano_hr_incomplete(UQ)=username, work_date

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `work_date` | nvarchar(20) | خیر | — |  |
| `first_in` | nvarchar(10) | بله | — |  |
| `reason` | nvarchar(400) | بله | — |  |
| `status` | nvarchar(40) | خیر | — | (N'open') |
| `fixed_out` | nvarchar(10) | بله | — |  |
| `fixed_by` | nvarchar(240) | بله | — |  |
| `fixed_at` | datetime2(8) | بله | — |  |
| `manager_note` | nvarchar(1000) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_hr_mission` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `reason` | nvarchar(400) | خیر | — |  |
| `details` | nvarchar(2000) | بله | — |  |
| `destination` | nvarchar(600) | بله | — |  |
| `expected_min` | int(4) | بله | — |  |
| `start_time` | datetime2(8) | خیر | — | (sysdatetime()) |
| `end_time` | datetime2(8) | بله | — |  |
| `start_lat` | float(8) | بله | — |  |
| `start_lng` | float(8) | بله | — |  |
| `end_lat` | float(8) | بله | — |  |
| `end_lng` | float(8) | بله | — |  |
| `start_bio` | bit(1) | بله | — |  |
| `end_bio` | bit(1) | بله | — |  |
| `status` | nvarchar(40) | خیر | — | (N'open') |
| `manager_status` | nvarchar(40) | خیر | — | (N'pending') |
| `manager_note` | nvarchar(1000) | بله | — |  |
| `decided_by` | nvarchar(240) | بله | — |  |
| `decided_at` | datetime2(8) | بله | — |  |

### `meelano_hr_month` — 6 ردیف (تخمین)
- کلیدها: PK_meelano_hr_month(PK)=username, jy, jm

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `username` | nvarchar(240) | خیر | — |  |
| `jy` | int(4) | خیر | — |  |
| `jm` | int(4) | خیر | — |  |
| `computed_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `present_days` | int(4) | بله | — |  |
| `absent_days` | int(4) | بله | — |  |
| `leave_days` | int(4) | بله | — |  |
| `incomplete_days` | int(4) | بله | — |  |
| `late_min` | int(4) | بله | — |  |
| `early_min` | int(4) | بله | — |  |
| `gap_min` | int(4) | بله | — |  |
| `overtime_min` | int(4) | بله | — |  |
| `night_min` | int(4) | بله | — |  |
| `holiday_min` | int(4) | بله | — |  |
| `mission_min` | int(4) | بله | — |  |
| `leave_min` | int(4) | بله | — |  |
| `gross` | bigint(8) | بله | — |  |
| `deduction` | bigint(8) | بله | — |  |
| `insurance` | bigint(8) | بله | — |  |
| `tax` | bigint(8) | بله | — |  |
| `net` | bigint(8) | بله | — |  |
| `detail` | nvarchar(-1) | بله | — |  |

### `meelano_hr_shift` — 1 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | — |  |
| `title` | nvarchar(240) | خیر | — |  |
| `grace_late` | int(4) | خیر | — | ((0)) |
| `grace_early` | int(4) | خیر | — | ((0)) |
| `overtime_min` | int(4) | خیر | — | ((0)) |
| `overtime_before_start` | bit(1) | خیر | — | ((0)) |
| `max_overtime_day` | int(4) | خیر | — | ((240)) |
| `night_start` | int(4) | خیر | — | ((1320)) |
| `night_end` | int(4) | خیر | — | ((360)) |
| `is_default` | bit(1) | خیر | — | ((0)) |

### `meelano_hr_shift_day` — 7 ردیف (تخمین)
- کلیدها: PK_meelano_hr_shift_day(PK)=shift_id, weekday

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shift_id` | int(4) | خیر | — |  |
| `weekday` | int(4) | خیر | — |  |
| `is_workday` | bit(1) | خیر | — |  |
| `start_min` | int(4) | خیر | — |  |
| `end_min` | int(4) | خیر | — |  |
| `rest_min` | int(4) | خیر | — | ((0)) |

### `meelano_hr_staff` — 3 ردیف (تخمین)
- کلیدها: <text 30>(PK)=username

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `username` | nvarchar(240) | خیر | — |  |
| `full_name` | nvarchar(400) | بله | — |  |
| `personnel_code` | nvarchar(80) | بله | — |  |
| `national_code` | nvarchar(40) | بله | — |  |
| `job_title` | nvarchar(240) | بله | — |  |
| `shift_id` | int(4) | بله | — |  |
| `daily_wage` | bigint(8) | بله | — |  |
| `housing` | bigint(8) | بله | — |  |
| `grocery` | bigint(8) | بله | — |  |
| `seniority_daily` | bigint(8) | بله | — |  |
| `married` | bit(1) | خیر | — | ((0)) |
| `children` | int(4) | خیر | — | ((0)) |
| `insured` | bit(1) | خیر | — | ((1)) |
| `hire_date` | nvarchar(20) | بله | — |  |
| `leave_carry_min` | int(4) | خیر | — | ((0)) |
| `active` | bit(1) | خیر | — | ((1)) |
| `note` | nvarchar(1000) | بله | — |  |
| `updated_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `updated_by` | nvarchar(240) | بله | — |  |

### `meelano_hr_zone` — 2 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `title` | nvarchar(400) | خیر | — |  |
| `kind` | nvarchar(20) | خیر | — |  |
| `lat` | float(8) | بله | — |  |
| `lng` | float(8) | بله | — |  |
| `radius_m` | int(4) | بله | — |  |
| `ssid` | nvarchar(400) | بله | — |  |
| `bssid` | nvarchar(200) | بله | — |  |
| `gateway` | nvarchar(160) | بله | — |  |
| `active` | bit(1) | خیر | — | ((1)) |
| `created_by` | nvarchar(240) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |

### `meelano_leave_requests` — 2 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(440) | بله | — |  |
| `leave_type` | nvarchar(160) | خیر | — |  |
| `start_date` | nvarchar(60) | خیر | — |  |
| `end_date` | nvarchar(60) | خیر | — |  |
| `hours` | nvarchar(80) | بله | — |  |
| `reason` | nvarchar(1400) | بله | — |  |
| `status` | nvarchar(60) | خیر | — | (N'pending') |
| `manager_note` | nvarchar(1400) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `decided_at` | datetime2(8) | بله | — |  |

### `meelano_prefactor_items` — 17 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `prefactor_id` | bigint(8) | خیر | — |  |
| `product_code` | nvarchar(200) | بله | — |  |
| `product_name` | nvarchar(1000) | بله | — |  |
| `unit` | nvarchar(160) | بله | — |  |
| `qty` | decimal(9) | بله | — |  |
| `price` | decimal(9) | بله | — |  |
| `amount` | decimal(9) | بله | — |  |
| `stock_snapshot` | decimal(9) | بله | — |  |
| `pack_count` | decimal(9) | بله | — |  |
| `price_tier` | nvarchar(20) | بله | — |  |
| `line_discount` | decimal(9) | بله | — |  |
| `line_note` | nvarchar(1400) | بله | — |  |

### `meelano_prefactors` — 13 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `client_uuid` | nvarchar(160) | بله | — |  |
| `visitor_username` | nvarchar(320) | بله | — |  |
| `visitor_id` | nvarchar(160) | بله | — |  |
| `customer_code` | nvarchar(200) | بله | — |  |
| `customer_name` | nvarchar(500) | بله | — |  |
| `notes` | nvarchar(-1) | بله | — |  |
| `signature_data` | nvarchar(-1) | بله | — |  |
| `total_amount` | decimal(9) | بله | — |  |
| `subtotal_amount` | decimal(9) | بله | — |  |
| `global_discount` | decimal(9) | بله | — |  |
| `tax_percent` | decimal(5) | بله | — |  |
| `tax_amount` | decimal(9) | بله | — |  |
| `grand_total` | decimal(9) | بله | — |  |
| `status` | nvarchar(60) | خیر | — | (N'draft') |
| `approval_reason` | nvarchar(1400) | بله | — |  |
| `delivery_date` | nvarchar(80) | بله | — |  |
| `delivery_address` | nvarchar(1400) | بله | — |  |
| `settlement_type` | nvarchar(160) | بله | — |  |
| `payment_ref` | nvarchar(320) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `updated_at` | datetime2(8) | بله | — |  |
| `ready_for_invoice` | bit(1) | خیر | — | ((0)) |
| `invoice_status` | nvarchar(80) | بله | — |  |
| `invoice_number` | nvarchar(160) | بله | — |  |
| `invoice_at` | datetime2(8) | بله | — |  |
| `system_convert_note` | nvarchar(1400) | بله | — |  |
| `native_prefactor_table` | nvarchar(240) | بله | — |  |
| `native_prefactor_no` | nvarchar(240) | بله | — |  |
| `native_sync_at` | datetime2(8) | بله | — |  |

### `meelano_staff_user` — 2 ردیف (تخمین)
- کلیدها: <text 30>(PK)=username

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `username` | nvarchar(240) | خیر | — |  |
| `display_name` | nvarchar(400) | بله | — |  |
| `last_seen` | datetime2(8) | خیر | — | (sysdatetime()) |
| `active` | bit(1) | خیر | — | ((1)) |

### `meelano_tax_buyers` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=shmo

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shmo` | bigint(8) | خیر | — |  |
| `tob` | int(4) | بله | — |  |
| `bid` | nvarchar(40) | بله | — |  |
| `tinb` | nvarchar(40) | بله | — |  |
| `bpc` | nvarchar(40) | بله | — |  |
| `bbc` | nvarchar(40) | بله | — |  |
| `inty` | int(4) | بله | — |  |
| `updated_at` | datetime(8) | خیر | — | (getdate()) |
| `updated_by` | nvarchar(200) | بله | — |  |

### `meelano_tax_goods` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shka` | int(4) | خیر | — |  |
| `sstid` | nvarchar(40) | بله | — |  |
| `sstt` | nvarchar(800) | بله | — |  |
| `mu` | nvarchar(20) | بله | — |  |
| `vra` | decimal(5) | بله | — |  |
| `updated_at` | datetime(8) | خیر | — | (getdate()) |
| `updated_by` | nvarchar(200) | بله | — |  |

### `meelano_tax_invoices` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `doc_kind` | nvarchar(20) | خیر | — |  |
| `doc_no` | int(4) | خیر | — |  |
| `back_kind` | int(4) | بله | — |  |
| `rdf` | int(4) | بله | — |  |
| `chain_sale` | int(4) | بله | — |  |
| `ins` | int(4) | خیر | — |  |
| `inty` | int(4) | بله | — |  |
| `taxid` | nvarchar(80) | بله | — |  |
| `irtaxid` | nvarchar(80) | بله | — |  |
| `serial` | bigint(8) | بله | — |  |
| `memory_id` | nvarchar(40) | بله | — |  |
| `env` | nvarchar(24) | بله | — |  |
| `uid` | nvarchar(160) | بله | — |  |
| `reference_number` | nvarchar(400) | بله | — |  |
| `status` | nvarchar(60) | خیر | — |  |
| `kartable_status` | nvarchar(120) | بله | — |  |
| `errors` | nvarchar(-1) | بله | — |  |
| `warnings` | nvarchar(-1) | بله | — |  |
| `payload` | nvarchar(-1) | بله | — |  |
| `content_hash` | nvarchar(160) | بله | — |  |
| `indatim` | bigint(8) | بله | — |  |
| `tbill` | bigint(8) | بله | — |  |
| `tvam` | bigint(8) | بله | — |  |
| `customer_shmo` | bigint(8) | بله | — |  |
| `customer_name` | nvarchar(600) | بله | — |  |
| `doc_date` | nvarchar(24) | بله | — |  |
| `created_at` | datetime(8) | خیر | — | (getdate()) |
| `updated_at` | datetime(8) | بله | — |  |
| `user_name` | nvarchar(200) | بله | — |  |
| `note` | nvarchar(1000) | بله | — |  |

### `meelano_tax_serial` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=memory_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `memory_id` | nvarchar(40) | خیر | — |  |
| `last_serial` | bigint(8) | خیر | — |  |

### `meelano_tax_settings` — 14 ردیف (تخمین)
- کلیدها: <text 30>(PK)=k

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `k` | nvarchar(200) | خیر | — |  |
| `v` | nvarchar(-1) | بله | — |  |
| `updated_at` | datetime(8) | خیر | — | (getdate()) |
| `updated_by` | nvarchar(200) | بله | — |  |

### `meelano_tax_units` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=unit_name

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `unit_name` | nvarchar(120) | خیر | — |  |
| `mu` | nvarchar(20) | خیر | — |  |

### `meelano_visit_results` — 0 ردیف (تخمین)
- کلیدها: <text 30>(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | bigint(8) | خیر | بله |  |
| `visitor_username` | nvarchar(320) | بله | — |  |
| `visitor_id` | nvarchar(160) | بله | — |  |
| `customer_code` | nvarchar(200) | بله | — |  |
| `customer_name` | nvarchar(500) | بله | — |  |
| `result` | nvarchar(240) | بله | — |  |
| `notes` | nvarchar(1400) | بله | — |  |
| `created_at` | datetime2(8) | خیر | — | (sysdatetime()) |
| `lat` | float(8) | بله | — |  |
| `lng` | float(8) | بله | — |  |

### `naghdinegi` — 28 ردیف (تخمین)
- کلیدها: PK_naghdinegi(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `mojsan` | money(8) | خیر | — |  |
| `chkdar` | money(8) | خیر | — |  |
| `chkpar` | money(8) | خیر | — |  |
| `bankmoj` | money(8) | خیر | — |  |
| `moinbed` | money(8) | خیر | — |  |
| `moinbes` | money(8) | خیر | — |  |
| `workerbed` | money(8) | خیر | — |  |
| `workerbes` | money(8) | خیر | — |  |
| `visbed` | money(8) | خیر | — |  |
| `visbes` | money(8) | خیر | — |  |
| `dara` | money(8) | خیر | — |  |
| `total` | money(8) | خیر | — |  |
| `date__` | char(10) | خیر | — |  |
| `mal_bed` | money(8) | خیر | — | ((0)) |
| `mal_bes` | money(8) | خیر | — | ((0)) |
| `DateServer` | nvarchar(20) | بله | — |  |
| `DateMiladi` | datetime(8) | بله | — |  |
| `RowID` | int(4) | خیر | بله |  |
| `MoeinBedWithBlackList` | money(8) | خیر | — | ((0)) |
| `MoeinBesWithBlackList` | money(8) | خیر | — | ((0)) |

### `news` — 0 ردیف (تخمین)
- کلیدها: PK__news__3213E83F1F7A47E2(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `title` | nvarchar(200) | خیر | — |  |
| `description` | text(16) | خیر | — |  |
| `actionUrl` | varchar(500) | بله | — |  |
| `forAll` | bit(1) | بله | — | ((1)) |
| `expireDate` | varchar(10) | بله | — |  |
| `createDateTime` | varchar(10) | بله | — |  |
| `image` | image(16) | بله | — |  |

### `operatingSystem` — 0 ردیف (تخمین)
- کلیدها: PK_operatingSystem(PK)=operatingSystemID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `operatingSystemID` | int(4) | خیر | بله |  |
| `operatingSystemName` | nvarchar(400) | بله | — |  |
| `commment` | nvarchar(-1) | بله | — |  |

### `osystems` — 1 ردیف (تخمین)
- کلیدها: PK_osystems(PK)=rdf_system
- FK `FK_osystems_anbars`: AnbarRdf → anbars.rdf_anbar
- FK `FK_osystems_FactorType`: kind_f → FactorType.factorTypeID
- FK `FK_osystems_LevelFive`: LevelIdFive → LevelFive.LevelFiveId
- FK `FK_osystems_LevelSix`: LevelIdSix → LevelSix.LevelSixId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_system` | int(4) | خیر | بله |  |
| `name_System` | nvarchar(2000) | خیر | — |  |
| `Address` | nvarchar(2000) | بله | — |  |
| `tel1` | nvarchar(100) | بله | — |  |
| `tel2` | nvarchar(100) | بله | — |  |
| `fax` | nvarchar(100) | بله | — |  |
| `sh_sabt` | nvarchar(100) | بله | — |  |
| `c_egh` | nvarchar(100) | بله | — |  |
| `c_pos` | nvarchar(100) | بله | — |  |
| `kind_f` | int(4) | بله | — |  |
| `arm_` | image(16) | بله | — |  |
| `eteb` | money(8) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `AnbarRdf` | int(4) | خیر | — | ((1)) |
| `LevelIdFive` | int(4) | بله | — |  |
| `LevelIdSix` | int(4) | بله | — |  |
| `LineTransferCode` | nvarchar(100) | بله | — |  |
| `ActivtionCode` | nvarchar(100) | بله | — |  |

### `overal_setting` — 423 ردیف (تخمین)
- کلیدها: PK_overal_setting(PK)=id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `id` | int(4) | خیر | بله |  |
| `dis` | nvarchar(2000) | خیر | — |  |
| `value` | bigint(8) | خیر | — |  |
| `Address` | nvarchar(1000) | بله | — |  |
| `TabOrder` | int(4) | بله | — |  |
| `ExternalExplain` | nvarchar(1000) | بله | — |  |

### `padash` — 0 ردیف (تخمین)
- کلیدها: PK_padash(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `azdate` | int(4) | خیر | — |  |
| `todate` | int(4) | خیر | — |  |
| `perp` | float(8) | خیر | — |  |
| `kind` | int(4) | خیر | — |  |

### `postpone` — 0 ردیف (تخمین)
- کلیدها: PK_postpone(PK)=PostponeID
- FK `FK_postpone_task`: taskID → task.taskID
- FK `FK_postpone_user`: userID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `PostponeID` | int(4) | خیر | بله |  |
| `taskID` | int(4) | بله | — |  |
| `fromDueDate` | nvarchar(30) | بله | — |  |
| `toDueDate` | nvarchar(30) | بله | — |  |
| `userID` | int(4) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `prizePercent` — 0 ردیف (تخمین)
- کلیدها: PK_prizePercent(PK)=QuarterID, Shka, FromNum, ToNum, FromPrice, ToPrice, FromDate, ToDate, RdfCustGroup, RdfProvinc, RdfCity, RdfRegion, RdfMasir, SysID
- FK `FK_pricePercent_inventory`: Shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | bigint(8) | خیر | بله |  |
| `Shka` | bigint(8) | خیر | — |  |
| `FromNum` | int(4) | خیر | — | ((0)) |
| `ToNum` | int(4) | خیر | — | ((0)) |
| `FromPrice` | money(8) | خیر | — | ((0)) |
| `ToPrice` | money(8) | خیر | — | ((0)) |
| `FromDate` | char(10) | خیر | — |  |
| `ToDate` | char(10) | خیر | — |  |
| `Percent` | decimal(9) | خیر | — | ((0)) |
| `RdfCustGroup` | int(4) | خیر | — | ((0)) |
| `RdfProvinc` | int(4) | خیر | — | ((0)) |
| `RdfCity` | int(4) | خیر | — | ((0)) |
| `RdfRegion` | int(4) | خیر | — | ((0)) |
| `RdfMasir` | int(4) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — | ((0)) |
| `DoneDate` | char(10) | خیر | — |  |
| `UserID` | int(4) | خیر | — | ((0)) |
| `QuarterID` | int(4) | خیر | — | ((0)) |

### `prizePercentGroup` — 0 ردیف (تخمین)
- کلیدها: PK_prizePercentGroup(PK)=QuarterID, RdfKalaGroup, FromNum, ToNum, FromPrice, ToPrice, FromDate, ToDate, RdfCustGroup, RdfProvinc, RdfCity, RdfRegion, RdfMasir, SysID
- FK `FK_pricePercentGroup_kagroup`: RdfKalaGroup → kagroup.group_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | bigint(8) | خیر | بله |  |
| `RdfKalaGroup` | int(4) | خیر | — |  |
| `FromNum` | int(4) | خیر | — | ((0)) |
| `ToNum` | int(4) | خیر | — | ((0)) |
| `FromPrice` | money(8) | خیر | — | ((0)) |
| `ToPrice` | money(8) | خیر | — | ((0)) |
| `FromDate` | char(10) | خیر | — |  |
| `ToDate` | char(10) | خیر | — |  |
| `Percent` | decimal(9) | خیر | — | ((0)) |
| `RdfCustGroup` | int(4) | خیر | — | ((0)) |
| `RdfProvinc` | int(4) | خیر | — | ((0)) |
| `RdfCity` | int(4) | خیر | — | ((0)) |
| `RdfRegion` | int(4) | خیر | — | ((0)) |
| `RdfMasir` | int(4) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — | ((0)) |
| `DoneDate` | char(10) | خیر | — |  |
| `UserID` | int(4) | خیر | — | ((0)) |
| `SubEffect` | bit(1) | خیر | — | ((0)) |
| `TedadTanavo` | int(4) | خیر | — | ((0)) |
| `QuarterID` | int(4) | خیر | — | ((0)) |

### `prize_table` — 0 ردیف (تخمین)
- کلیدها: PK_prize_table(PK)=QuarterID, shka, ted_st, ted_end, CusGroup, SysID, ProvinceID, CityID, RegionID, PathID
- FK `FK_prize_table_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `shka` | int(4) | خیر | — |  |
| `ted_st` | int(4) | خیر | — |  |
| `ted_end` | int(4) | خیر | — |  |
| `shkaja` | int(4) | خیر | — |  |
| `nakaja` | varchar(500) | خیر | — |  |
| `tedja` | int(4) | خیر | — |  |
| `CusGroup` | int(4) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — | ((0)) |
| `ProvinceID` | int(4) | خیر | — | ((0)) |
| `CityID` | int(4) | خیر | — | ((0)) |
| `RegionID` | int(4) | خیر | — | ((0)) |
| `PathID` | int(4) | خیر | — | ((0)) |
| `Date` | nvarchar(20) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `QuarterID` | int(4) | خیر | — | ((0)) |

### `prize_table_group` — 0 ردیف (تخمین)
- کلیدها: PK_prize_table_group(PK)=QuarterID, rdf_group, ted_st, ted_end, CusGroup, SysID, ProvinceID, CityID, RegionID, PathID
- FK `<text 30>`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `rdf_group` | int(4) | خیر | — |  |
| `ted_st` | int(4) | خیر | — |  |
| `ted_end` | int(4) | خیر | — |  |
| `shkaja` | int(4) | خیر | — |  |
| `nakaja` | nvarchar(1000) | خیر | — |  |
| `tedja` | int(4) | خیر | — |  |
| `CusGroup` | int(4) | خیر | — | ((0)) |
| `SysID` | int(4) | خیر | — | ((0)) |
| `ProvinceID` | int(4) | خیر | — | ((0)) |
| `CityID` | int(4) | خیر | — | ((0)) |
| `RegionID` | int(4) | خیر | — | ((0)) |
| `PathID` | int(4) | خیر | — | ((0)) |
| `Date` | nvarchar(20) | بله | — |  |
| `SubEffect` | bit(1) | خیر | — | ((0)) |
| `UserID` | int(4) | خیر | — |  |
| `TedadTanavo` | int(4) | خیر | — | ((0)) |
| `QuarterID` | int(4) | خیر | — | ((0)) |

### `product` — 0 ردیف (تخمین)
- کلیدها: PK_product(PK)=productID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `productID` | int(4) | خیر | بله |  |
| `name` | nvarchar(600) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `project` — 0 ردیف (تخمین)
- کلیدها: PK_Projects(PK)=ProjectID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ProjectID` | int(4) | خیر | بله |  |
| `ProjectName` | nvarchar(200) | بله | — |  |
| `ParentProjectID` | int(4) | بله | — |  |
| `startDate` | nvarchar(20) | بله | — |  |
| `endDate` | nvarchar(20) | بله | — |  |
| `ParentProjectName` | nvarchar(200) | بله | — |  |

### `putchk` — 202 ردیف (تخمین)
- کلیدها: PK_putchk(PK)=rdf
- FK `FK_putchk_BANK`: bankrdf → BANK.RDF
- FK `FK_putchk_CheckTypes`: CheckTypeID → CheckTypes.ID
- FK `FK_putchk_chkbatch`: bankrdf → chkbatch.rdf_bank
- FK `FK_putchk_chkbatch`: chkbatch_num → chkbatch.chkbatch_num
- FK `FK_putchk_Moein`: MoeinId → Moein.MoeinID
- FK `FK_putchk_sys_users`: UserID → sys_users.user_id
- FK `FK_putchk_Tafsil`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | bigint(8) | خیر | بله |  |
| `putdate` | char(10) | خیر | — |  |
| `sardate` | char(10) | خیر | — |  |
| `shputchk` | varchar(30) | خیر | — |  |
| `bankrdf` | int(4) | خیر | — |  |
| `putchkmab` | money(8) | خیر | — |  |
| `shmo` | bigint(8) | خیر | — | ((0)) |
| `putchkdis` | varchar(500) | خیر | — |  |
| `putchk_status` | int(4) | خیر | — |  |
| `shfacbuy` | bigint(8) | خیر | — | ((0)) |
| `girande` | varchar(500) | خیر | — |  |
| `ghno` | bigint(8) | خیر | — | ((0)) |
| `rdf_in_ghno` | int(4) | خیر | — | ((0)) |
| `done_date` | char(10) | بله | — |  |
| `mod` | int(4) | بله | — |  |
| `amani` | char(1) | بله | — |  |
| `chkbatch_num` | int(4) | بله | — |  |
| `DocID` | bigint(8) | بله | — |  |
| `FromAtiranDocID` | bigint(8) | بله | — |  |
| `Desc` | nvarchar(1000) | بله | — |  |
| `Rdf_` | int(4) | خیر | — | ((1)) |
| `TafsilID` | bigint(8) | بله | — |  |
| `UserID` | int(4) | خیر | — |  |
| `ShenaseSayad` | nvarchar(100) | بله | — |  |
| `RegistrationInquiry` | bit(1) | خیر | — | ((0)) |
| `IdDocumentZirSarfasl` | int(4) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — |  |
| `DateOfReceipt` | char(10) | بله | — |  |
| `CheckTypeID` | int(4) | خیر | — | ((1)) |

### `regions` — 3 ردیف (تخمین)
- کلیدها: PK_regions(PK)=rdf_region
- FK `FK_regions_CITYS`: rdf_city → CITYS.RDF

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf_region` | int(4) | خیر | بله |  |
| `rdf_city` | int(4) | خیر | — |  |
| `sh_region` | int(4) | خیر | — |  |
| `name_region` | varchar(50) | خیر | — |  |
| `Color` | int(4) | بله | — |  |
| `Code` | nvarchar(1000) | خیر | — |  |

### `role` — 8 ردیف (تخمین)
- کلیدها: PK_Rolls(PK)=RollID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RollID` | int(4) | خیر | بله |  |
| `RollName` | nvarchar(200) | بله | — |  |

### `sailfact` — 1168 ردیف (تخمین)
- کلیدها: PK_sailfact(PK)=rdf__, shfacfo
- FK `FK_sailfact_CUSTOMERS`: shmo → CUSTOMERS.SHMO
- FK `<text 30>`: DocumentSourceID → DocumentSourceKind.ID
- FK `FK_sailfact_sys_users`: userid → sys_users.user_id
- FK `FK_sailfact_sys_users1`: TaeedWarehousUserId → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf__` | int(4) | خیر | — |  |
| `shfacfo` | bigint(8) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `shmo` | int(4) | خیر | — |  |
| `barbari` | money(8) | بله | — |  |
| `description` | nvarchar(1000) | بله | — | ('ذکر نشده') |
| `vis_rdf` | int(4) | خیر | — |  |
| `sumlineall` | money(8) | خیر | — |  |
| `all` | money(8) | خیر | — |  |
| `tafif` | money(8) | خیر | — |  |
| `SumTafifAghlam` | money(8) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `panevis` | nvarchar(2000) | خیر | — |  |
| `ismodify` | char(1) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `modpar` | int(4) | خیر | — |  |
| `rdf_tahbarg` | int(4) | خیر | — |  |
| `nah_par` | int(4) | خیر | — |  |
| `nah_d_text` | nvarchar(400) | خیر | — | ('عندالمطالبه') |
| `man_gh` | money(8) | بله | — |  |
| `t_date` | char(10) | بله | — |  |
| `tasvieh` | char(1) | بله | — |  |
| `driver_name` | varchar(70) | بله | — |  |
| `rdf_driver` | int(4) | بله | — |  |
| `mamorp_name` | varchar(70) | بله | — |  |
| `rdf_mamorp` | int(4) | بله | — |  |
| `bamandeh` | int(4) | بله | — |  |
| `sh_taraz_kh` | int(4) | بله | — |  |
| `time_` | varchar(50) | بله | — |  |
| `shpish` | nvarchar(1000) | بله | — |  |
| `ba_tarikh` | int(4) | بله | — |  |
| `chap_f` | bit(1) | بله | — |  |
| `chap_h` | bit(1) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `moname` | nvarchar(1000) | بله | — |  |
| `nahve_namayesh_daryaft` | int(4) | بله | — |  |
| `vazn` | decimal(9) | بله | — | ((0)) |
| `sysid` | int(4) | بله | — | ((1)) |
| `avarez` | money(8) | بله | — | ((0)) |
| `Status` | int(4) | بله | — | ((0)) |
| `TaeedDate` | nvarchar(20) | بله | — |  |
| `TaeedUser` | nvarchar(60) | بله | — |  |
| `sumlineall_fel` | money(8) | بله | — |  |
| `all_fel` | money(8) | بله | — | ((0)) |
| `SumTafifAghlam_fel` | money(8) | بله | — |  |
| `tax_fel` | money(8) | بله | — |  |
| `avarez_fel` | money(8) | بله | — |  |
| `userid` | int(4) | خیر | — |  |
| `taffif_fel` | money(8) | بله | — |  |
| `MabDaryaftFactor` | money(8) | بله | — |  |
| `tdf` | money(8) | بله | — |  |
| `ShSanadFerestande` | nvarchar(1000) | بله | — |  |
| `ExternalCosts` | decimal(9) | بله | — |  |
| `HajmiOverall` | decimal(9) | بله | — |  |
| `ChapWithTasvieh` | bit(1) | خیر | — | ((1)) |
| `ChapWhitMande` | bit(1) | خیر | — | ((1)) |
| `TaeedWarehous` | bit(1) | خیر | — | ((0)) |
| `TaeedWarehousUserId` | int(4) | بله | — |  |
| `TaeedWarehousDate` | char(10) | بله | — |  |
| `TaxUniqueID` | nvarchar(100) | بله | — |  |
| `SubmittedTax` | int(4) | خیر | — | ((0)) |
| `MsgErrorTax` | nvarchar(-1) | بله | — |  |
| `DateSendToTaxSystem` | nchar(20) | بله | — |  |
| `TimeSendToTaxSystem` | nchar(20) | بله | — |  |
| `UidTax` | nvarchar(200) | بله | — |  |
| `InvoiceSerialTax` | nvarchar(100) | بله | — |  |
| `SubmitTaxText` | nvarchar(1000) | بله | — |  |
| `DescriptionDelete` | nvarchar(-1) | بله | — |  |
| `Deleted` | bit(1) | خیر | — | ((0)) |
| `TypeInvoiceSentToMoadiyan` | int(4) | بله | — |  |
| `TaxRefrenceNumber` | nvarchar(1000) | بله | — |  |
| `TaxManualDate` | nvarchar(20) | بله | — |  |
| `FiscalId` | nvarchar(100) | بله | — |  |
| `UniqueID` | nvarchar(100) | بله | — |  |
| `isSync` | bit(1) | خیر | — | ((0)) |
| `DocumentSourceID` | int(4) | خیر | — | ((1)) |

### `sailfact_pish` — 13 ردیف (تخمین)
- کلیدها: PK_sailfact_pish(PK)=rdf__, shfacfo
- FK `FK_sailfact_pish_CUSTOMERS`: shmo → CUSTOMERS.SHMO
- FK `FK_sailfact_pish_visit`: VisitID → Visit.VisitID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf__` | int(4) | خیر | — |  |
| `shfacfo` | bigint(8) | خیر | — |  |
| `USER__` | varchar(300) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `shmo` | int(4) | خیر | — |  |
| `barbari` | money(8) | بله | — |  |
| `shfacthand` | nvarchar(1000) | بله | — | ('ذکر نشده') |
| `vis_rdf` | int(4) | خیر | — |  |
| `sumlineall` | money(8) | خیر | — |  |
| `all` | money(8) | خیر | — |  |
| `gainall` | money(8) | خیر | — |  |
| `tafif` | money(8) | خیر | — |  |
| `jamtakhgh` | money(8) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `panevis` | varchar(80) | خیر | — |  |
| `isret` | char(1) | خیر | — |  |
| `ismodify` | char(1) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `modpar` | int(4) | خیر | — |  |
| `rdf_sarbarg` | int(4) | خیر | — |  |
| `rdf_tahbarg` | int(4) | خیر | — |  |
| `nah_par` | int(4) | خیر | — |  |
| `mod_darsad_vis` | int(4) | خیر | — |  |
| `nah_d_text` | nvarchar(1000) | خیر | — | ('اعتباري') |
| `man_gh` | money(8) | بله | — |  |
| `sh_f` | bigint(8) | بله | — |  |
| `user_f` | varchar(300) | بله | — |  |
| `date_f` | char(10) | بله | — |  |
| `ted_rooz` | int(4) | بله | — |  |
| `taeed` | int(4) | بله | — | ((0)) |
| `taeedUser` | varchar(300) | بله | — | ('--') |
| `sysid` | int(4) | بله | — | ((1)) |
| `TaedHesabdari` | bit(1) | خیر | — | ((0)) |
| `TaedForush` | bit(1) | خیر | — | ((0)) |
| `UserTaedHesabdari` | nvarchar(1000) | بله | — |  |
| `DateTaedHesabdari` | nvarchar(20) | بله | — |  |
| `UserTaedForush` | nvarchar(100) | بله | — |  |
| `DateTaedForush` | nvarchar(20) | بله | — |  |
| `VisitID` | bigint(8) | بله | — |  |
| `Stamp` | nvarchar(1000) | بله | — |  |
| `Rejected` | bit(1) | بله | — |  |
| `RejectedUser` | nvarchar(1000) | بله | — |  |
| `RejectedDate` | nvarchar(20) | بله | — |  |
| `RejectedComment` | nvarchar(2000) | بله | — |  |
| `taraz_kh_pish` | int(4) | بله | — |  |
| `DateRecive` | varchar(10) | بله | — |  |
| `TimeRecive` | varchar(20) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `avarez` | money(8) | بله | — |  |
| `MpKol` | int(4) | بله | — |  |
| `MpIsAuto` | bit(1) | بله | — |  |
| `Promotion` | money(8) | خیر | — | ((0)) |

### `sal_mali` — 1 ردیف (تخمین)
- کلیدها: PK_sal_mali(PK)=sal_maliID
- FK `FK_sal_mali_Company`: rdf → Company.rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `sal_maliID` | int(4) | خیر | بله |  |
| `rdf` | int(4) | خیر | — |  |
| `name` | varchar(100) | خیر | — |  |
| `nam_db` | varchar(100) | خیر | — |  |
| `StartDate` | datetime(8) | بله | — |  |
| `EndDate` | datetime(8) | بله | — |  |
| `Current` | bit(1) | بله | — | ((0)) |

### `sanad_enteghal` — 0 ردیف (تخمین)
- کلیدها: PK_sanad_enteghal(PK)=sh_sanad, Rdf_
- FK `FK_sanad_enteghal_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `sh_sanad` | int(4) | خیر | — |  |
| `sharh` | varchar(200) | بله | — |  |
| `date_` | char(10) | خیر | — |  |
| `done_date` | char(10) | خیر | — |  |
| `sysid` | int(4) | خیر | — | ((1)) |
| `UserID` | int(4) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `Rdf_` | bigint(8) | خیر | — | ((1)) |
| `Hour` | char(8) | بله | — |  |
| `Decided` | bit(1) | خیر | — | ((0)) |
| `isSync` | bit(1) | خیر | — | ((0)) |

### `sar_tah_barg` — 1 ردیف (تخمین)
- کلیدها: PK_sar_tah_barg(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `f_line` | varchar(100) | خیر | — |  |
| `sar_tah` | int(4) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `mohtava` | varchar(1000) | خیر | — | ('ذکر نشده') |

### `sarfasls` — 12 ردیف (تخمین)
- کلیدها: PK_sarfasls(PK)=rdf
- FK `FK_sarfasls_GroupSarfasl`: GroupSarfaslID → GroupSarfasl.GroupSarfaslID
- FK `FK_sarfasls_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `name` | nvarchar(-1) | خیر | — |  |
| `start_date` | char(10) | بله | — |  |
| `Active` | bit(1) | بله | — |  |
| `GroupSarfaslID` | int(4) | خیر | — | ((1)) |
| `UserID` | int(4) | خیر | — |  |

### `sms_table` — 0 ردیف (تخمین)
- کلیدها: PK_sms_table(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `shmo` | bigint(8) | بله | — |  |
| `moname` | nvarchar(200) | بله | — |  |
| `moh` | nvarchar(8000) | خیر | — |  |
| `cell` | nvarchar(100) | خیر | — |  |
| `user_` | nvarchar(600) | بله | — |  |
| `status` | int(4) | خیر | — |  |
| `date_` | char(10) | بله | — |  |
| `deleted` | int(4) | بله | — | (0) |
| `deleteUser` | nvarchar(100) | بله | — | ('--') |
| `uid` | bigint(8) | بله | — |  |
| `ErrorComment` | nvarchar(1000) | بله | — |  |
| `isPattern` | bit(1) | خیر | — | ((0)) |
| `TemplateName` | nvarchar(100) | بله | — |  |
| `Parameter1` | nvarchar(600) | بله | — |  |
| `Parameter2` | nvarchar(600) | بله | — |  |
| `Parameter3` | nvarchar(600) | بله | — |  |
| `Parameter4` | nvarchar(600) | بله | — |  |
| `Parameter5` | nvarchar(600) | بله | — |  |
| `Parameter6` | nvarchar(600) | بله | — |  |
| `Parameter7` | nvarchar(600) | بله | — |  |
| `Parameter8` | nvarchar(600) | بله | — |  |
| `Parameter9` | nvarchar(600) | بله | — |  |
| `Parameter10` | nvarchar(600) | بله | — |  |

### `subAnbarSanad` — 0 ردیف (تخمین)
- کلیدها: PK_subAnbarSenad(PK)=RowId
- FK `FK_subAnbarSanad_inventory`: shka → inventory.shka
- FK `FK_subAnbarSanad_sys_users`: UserId → sys_users.user_id
- FK `FK_subAnbarSenad_AnbarSenad`: anbarSanadID → AnbarSanad.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowId` | bigint(8) | خیر | بله |  |
| `anbarSanadID` | bigint(8) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `tedVah` | decimal(9) | خیر | — |  |
| `tedJoz` | int(4) | خیر | — |  |
| `anbarID` | int(4) | خیر | — |  |
| `psID` | bigint(8) | خیر | — |  |
| `Description` | nvarchar(1000) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `UserId` | int(4) | بله | — |  |
| `Date` | char(10) | بله | — |  |
| `Time` | char(10) | بله | — |  |

### `subBackSail_pish` — 0 ردیف (تخمین)
- کلیدها: PK_subBackSail_pish(PK)=rowId
- FK `<text 33>`: shBackSail → backsail_pish.RowId
- FK `<text 29>`: Shka → inventory.shka

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rowId` | bigint(8) | خیر | بله |  |
| `shBackSail` | bigint(8) | خیر | — |  |
| `Shka` | bigint(8) | خیر | — |  |
| `Tedvah` | decimal(9) | خیر | — |  |
| `Tedjoz` | int(4) | خیر | — |  |
| `RdfLineInSail` | int(4) | خیر | — |  |
| `Details` | nvarchar(2000) | خیر | — |  |

### `sub_buyfact_temp` — 0 ردیف (تخمین)
- کلیدها: PK_sub_buyfact_temp(PK)=shfackh, rdf, rdf__

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfackh` | bigint(8) | خیر | — |  |
| `rdf` | int(4) | خیر | — |  |
| `shka` | bigint(8) | بله | — |  |
| `tedvah` | decimal(9) | بله | — |  |
| `tedjoz` | int(4) | بله | — |  |
| `vahprice` | money(8) | بله | — |  |
| `jozprice` | money(8) | بله | — |  |
| `bastebandi` | nvarchar(1000) | بله | — |  |
| `tedbastebandi` | decimal(9) | بله | — |  |
| `linesum` | money(8) | بله | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `pertafif` | decimal(9) | بله | — |  |
| `litakhma` | money(8) | بله | — |  |
| `active` | char(1) | بله | — |  |
| `joz_gh` | int(4) | بله | — |  |
| `vah_gh` | decimal(9) | بله | — |  |
| `invent_price_gh` | money(8) | بله | — |  |
| `date` | char(10) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `moh_vah` | int(4) | بله | — |  |
| `hajm_ka` | decimal(9) | بله | — |  |
| `vis_dar_ka` | decimal(9) | بله | — |  |
| `reopoint` | int(4) | بله | — |  |
| `ka_na` | nvarchar(1000) | بله | — |  |
| `forosh1` | decimal(9) | بله | — |  |
| `forosh2` | decimal(9) | بله | — |  |
| `forosh3` | decimal(9) | بله | — |  |
| `forosh4` | decimal(9) | بله | — |  |
| `forosh5` | decimal(9) | بله | — |  |
| `tamam_joz` | money(8) | بله | — |  |
| `act_id` | int(4) | بله | — |  |
| `act_dis` | nvarchar(1000) | بله | — |  |
| `rdf_anb_m` | int(4) | بله | — |  |
| `time_` | int(4) | بله | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `tamam_joz_tax` | money(8) | بله | — |  |
| `PAvarez` | decimal(9) | بله | — |  |
| `Avarez` | money(8) | بله | — |  |
| `PPromotion` | decimal(9) | بله | — |  |
| `Promotion` | money(8) | بله | — |  |
| `SysID` | int(4) | خیر | — | ((1)) |
| `HajmiTafif2` | decimal(9) | خیر | — | ((0)) |
| `MaxPrice` | decimal(9) | بله | — |  |
| `MinPrice` | decimal(9) | بله | — |  |
| `pv1` | decimal(9) | بله | — |  |
| `pv2` | decimal(9) | بله | — |  |
| `pv3` | decimal(9) | بله | — |  |
| `pv4` | decimal(9) | بله | — |  |
| `pv5` | decimal(9) | بله | — |  |
| `CustomerPrice` | decimal(9) | بله | — |  |
| `HajmiTafif1` | decimal(9) | خیر | — | ((0)) |
| `UserID` | int(4) | بله | — |  |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `PerPos` | decimal(9) | بله | — |  |
| `maxtafnaghd` | decimal(9) | بله | — |  |
| `Desc_Naka` | nvarchar(1000) | بله | — |  |
| `ExpirationDate` | char(10) | بله | — |  |
| `VarietyID` | int(4) | خیر | — |  |

### `subbuyfact` — 211 ردیف (تخمین)
- کلیدها: PK_subbuyfact(PK)=shfackh, rdf, rdf__
- FK `FK_subbuyfact_buyfact1`: shfackh → buyfact.shfackh
- FK `FK_subbuyfact_buyfact1`: rdf__ → buyfact.rdf__
- FK `FK_subbuyfact_Variety`: VarietyID → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfackh` | bigint(8) | خیر | — |  |
| `rdf` | int(4) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `TEDVAH` | decimal(9) | خیر | — |  |
| `TEDJOZ` | int(4) | بله | — |  |
| `JOZPRICE` | money(8) | خیر | — |  |
| `TEDBASTEBANDI` | decimal(9) | بله | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `pertafif` | decimal(9) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `tamam_joz` | money(8) | خیر | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tamam_joz_tax` | money(8) | بله | — |  |
| `pPromotion` | decimal(9) | بله | — |  |
| `pAvarez` | decimal(9) | بله | — |  |
| `rdf_anbar` | int(4) | بله | — |  |
| `TafifHajmi2` | decimal(9) | خیر | — | ((0)) |
| `TafifHajmi1` | decimal(9) | خیر | — | ((0)) |
| `TedJozFel` | int(4) | خیر | — | ((0)) |
| `TafifHajmi1Fel` | decimal(9) | خیر | — | ((0)) |
| `TafifHajmi2Fel` | decimal(9) | خیر | — | ((0)) |
| `TedvahFel` | decimal(9) | خیر | — | ((0)) |
| `TedBasteBandiFel` | decimal(9) | خیر | — | ((0)) |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `Desc_Naka` | nvarchar(1000) | خیر | — | ('') |
| `VarietyID` | int(4) | خیر | — |  |

### `subbuyfact_pish` — 0 ردیف (تخمین)
- کلیدها: PK_subbuyfact_pish(PK)=shfackh, rdf, rdf__
- FK `<text 34>`: shfackh → buyfact_pish.shfackh
- FK `<text 34>`: rdf__ → buyfact_pish.rdf__

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfackh` | bigint(8) | خیر | — |  |
| `rdf` | int(4) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `TEDVAH` | decimal(9) | خیر | — |  |
| `TEDJOZ` | int(4) | بله | — |  |
| `VAHPRICE` | money(8) | خیر | — |  |
| `JOZPRICE` | money(8) | خیر | — |  |
| `BASTEBANDI` | varchar(25) | بله | — |  |
| `TEDBASTEBANDI` | decimal(9) | بله | — |  |
| `LINESUM` | money(8) | خیر | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `pertafif` | decimal(9) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `litakhma` | money(8) | خیر | — |  |
| `joz_gh` | int(4) | خیر | — |  |
| `vah_gh` | decimal(9) | خیر | — |  |
| `invent_price_gh` | money(8) | خیر | — |  |
| `tamam_joz` | money(8) | خیر | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `tamam_joz_tax` | money(8) | بله | — |  |
| `pPromotion` | decimal(9) | بله | — |  |
| `Promotion` | money(8) | بله | — |  |
| `pAvarez` | decimal(9) | بله | — |  |
| `Avarez` | money(8) | بله | — |  |
| `rdf_anbar` | int(4) | بله | — |  |
| `naka` | nvarchar(1000) | بله | — |  |
| `mojkavah` | decimal(9) | بله | — |  |
| `mojkajoz` | int(4) | بله | — |  |
| `anbarName` | nvarchar(1000) | بله | — |  |
| `vahsanj` | nvarchar(1000) | بله | — |  |
| `mohvah` | bigint(8) | بله | — |  |

### `subsailfact` — 4991 ردیف (تخمین)
- کلیدها: pk_subsailfact(PK)=rdf__, shfacfo, RDF
- FK `FK_subsailfact_anbars`: rdf_anbar → anbars.rdf_anbar
- FK `FK_subsailfact_inventory`: SHKA → inventory.shka
- FK `FK_subsailfact_sailfact`: rdf__ → sailfact.rdf__
- FK `FK_subsailfact_sailfact`: shfacfo → sailfact.shfacfo
- FK `FK_subsailfact_Variety`: VarietyID → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf__` | int(4) | خیر | — |  |
| `shfacfo` | bigint(8) | خیر | — |  |
| `SHKA` | bigint(8) | خیر | — |  |
| `rdf_anbar` | int(4) | خیر | — |  |
| `TEDVAH` | decimal(9) | خیر | — |  |
| `TEDJOZ` | int(4) | بله | — |  |
| `VAHPRICE` | money(8) | خیر | — |  |
| `JOZPRICE` | money(8) | خیر | — |  |
| `BASTEBANDI` | varchar(25) | بله | — |  |
| `TEDBASTEBANDI` | decimal(9) | بله | — |  |
| `LINESUM` | money(8) | خیر | — |  |
| `PERTAFIF` | decimal(9) | بله | — |  |
| `TafifAghlam` | money(8) | بله | — |  |
| `RDF` | int(4) | خیر | — |  |
| `PERVIS` | decimal(9) | بله | — |  |
| `litakhma` | money(8) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `naka` | nvarchar(1000) | بله | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `avarez` | money(8) | بله | — | ((0)) |
| `pavarez` | decimal(9) | بله | — |  |
| `tedjoz_fel` | int(4) | بله | — |  |
| `tedvah_fel` | decimal(9) | بله | — |  |
| `tedbastebandi_fel` | decimal(9) | بله | — |  |
| `linesum_fel` | money(8) | بله | — |  |
| `litakhma_fel` | money(8) | بله | — |  |
| `tax_fel` | money(8) | بله | — |  |
| `avarez_fel` | money(8) | بله | — |  |
| `Gift` | bit(1) | بله | — |  |
| `TafifLine` | money(8) | خیر | — | ((0)) |
| `PerPromotion` | decimal(9) | بله | — |  |
| `PromotionValue` | decimal(9) | بله | — |  |
| `TafifLineFel` | decimal(9) | خیر | — | ((0)) |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `TEDVAHMain` | decimal(9) | خیر | — |  |
| `TEDJOZMain` | int(4) | خیر | — |  |
| `TafifPos` | decimal(9) | خیر | — | ((0)) |
| `TafifNaghd` | decimal(9) | خیر | — | ((0)) |
| `VarietyID` | int(4) | خیر | — |  |

### `subsailfact_pish` — 15 ردیف (تخمین)
- کلیدها: pk_subsailfact_pish(PK)=rdf__, shfacfo, RDF
- FK `<text 33>`: rdf__ → sailfact_pish.rdf__
- FK `<text 33>`: shfacfo → sailfact_pish.shfacfo

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf__` | int(4) | خیر | — |  |
| `shfacfo` | bigint(8) | خیر | — |  |
| `SHKA` | bigint(8) | خیر | — |  |
| `rdf_anbar` | int(4) | خیر | — |  |
| `TEDVAH` | decimal(9) | خیر | — |  |
| `TEDJOZ` | int(4) | بله | — |  |
| `VAHPRICE` | money(8) | خیر | — |  |
| `JOZPRICE` | money(8) | خیر | — |  |
| `BASTEBANDI` | varchar(25) | بله | — |  |
| `TEDBASTEBANDI` | int(4) | بله | — |  |
| `LINESUM` | money(8) | خیر | — |  |
| `LINEGAIN` | money(8) | خیر | — |  |
| `ISRET` | char(1) | خیر | — |  |
| `PERTAFIF` | decimal(9) | بله | — |  |
| `RDF` | int(4) | خیر | — |  |
| `jozgain` | money(8) | خیر | — |  |
| `PERVIS` | decimal(9) | بله | — |  |
| `litakhma` | money(8) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `amani` | bit(1) | بله | — | (0) |
| `Pavarez` | decimal(9) | بله | — |  |
| `Avarez` | money(8) | بله | — |  |
| `Ptax` | decimal(9) | بله | — |  |
| `Tax` | money(8) | بله | — |  |
| `Mp` | int(4) | بله | — |  |
| `PerPromotion` | decimal(9) | بله | — |  |

### `subsailtemp` — 0 ردیف (تخمین)
- کلیدها: pk_subsailtemp(PK)=shfacfo, rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfacfo` | bigint(8) | خیر | — |  |
| `shka` | bigint(8) | بله | — |  |
| `rdf_anbar` | int(4) | بله | — |  |
| `tedvah` | decimal(9) | بله | — |  |
| `tedjoz` | int(4) | بله | — |  |
| `vahprice` | money(8) | بله | — |  |
| `jozprice` | money(8) | بله | — |  |
| `bastebandi` | nvarchar(500) | بله | — |  |
| `tedbastebandi` | decimal(9) | بله | — |  |
| `linesum` | money(8) | بله | — |  |
| `isret` | char(1) | بله | — |  |
| `pertafif` | decimal(9) | بله | — |  |
| `tafifAghlam` | money(8) | بله | — |  |
| `rdf` | int(4) | خیر | — |  |
| `pervis` | decimal(9) | بله | — |  |
| `litakhma` | money(8) | بله | — |  |
| `mohvah` | int(4) | بله | — |  |
| `modpar` | int(4) | بله | — |  |
| `mod` | int(4) | بله | — |  |
| `rdf__` | int(4) | بله | — |  |
| `active` | char(1) | بله | — |  |
| `date` | char(10) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `sahm_mod_par` | decimal(9) | بله | — |  |
| `naka` | nvarchar(2000) | بله | — |  |
| `ted_kol` | decimal(9) | بله | — |  |
| `vah_nam` | nvarchar(600) | بله | — |  |
| `amani` | int(4) | بله | — |  |
| `invepgh` | money(8) | بله | — |  |
| `vah_w` | decimal(9) | بله | — |  |
| `sood_gh` | money(8) | بله | — |  |
| `vis_sahm` | money(8) | بله | — |  |
| `vis_rdf` | int(4) | بله | — |  |
| `time_` | int(4) | بله | — |  |
| `ptax` | decimal(9) | بله | — |  |
| `tax` | money(8) | بله | — |  |
| `avarez` | money(8) | بله | — | ((0)) |
| `PAvarez` | decimal(9) | بله | — |  |
| `sahmtaf` | decimal(9) | بله | — |  |
| `sysid` | int(4) | بله | — |  |
| `Gift` | bit(1) | بله | — |  |
| `TafifLine` | money(8) | خیر | — | ((0)) |
| `PerPromotion` | decimal(9) | بله | — |  |
| `PromotionValue` | decimal(9) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `ProductionSeriesID` | bigint(8) | بله | — |  |
| `TEDVAHMain` | decimal(9) | خیر | — |  |
| `TEDJOZMain` | int(4) | خیر | — |  |
| `MultiPishFactor` | bit(1) | خیر | — | ((0)) |
| `TafifPos` | decimal(9) | خیر | — | ((0)) |
| `TafifNaghd` | decimal(9) | خیر | — | ((0)) |
| `VarietyID` | int(4) | بله | — |  |

### `subsailtemp_pish` — 0 ردیف (تخمین)
- کلیدها: pk_subsailtemp_pish(PK)=shfacfo, rdf, rdf__

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `shfacfo` | bigint(8) | خیر | — |  |
| `shka` | bigint(8) | بله | — |  |
| `rdf_anbar` | int(4) | بله | — |  |
| `tedvah` | decimal(9) | بله | — |  |
| `tedjoz` | int(4) | بله | — |  |
| `vahprice` | money(8) | بله | — |  |
| `jozprice` | money(8) | بله | — |  |
| `bastebandi` | varchar(25) | بله | — |  |
| `tedbastebandi` | int(4) | بله | — |  |
| `linesum` | money(8) | بله | — |  |
| `isret` | char(1) | بله | — |  |
| `pertafif` | decimal(9) | بله | — |  |
| `rdf` | int(4) | خیر | — |  |
| `pervis` | decimal(9) | بله | — |  |
| `litakhma` | money(8) | بله | — |  |
| `mohvah` | int(4) | بله | — |  |
| `modpar` | int(4) | بله | — |  |
| `mod` | int(4) | بله | — |  |
| `rdf__` | int(4) | خیر | — |  |
| `active` | char(1) | بله | — |  |
| `date` | char(10) | بله | — |  |
| `done_date` | char(10) | بله | — |  |
| `sahm_mod_par` | decimal(9) | بله | — |  |
| `naka` | nvarchar(1000) | بله | — |  |
| `ted_kol` | decimal(9) | بله | — |  |
| `vah_nam` | varchar(30) | بله | — |  |
| `amani` | int(4) | بله | — |  |
| `invepgh` | money(8) | بله | — |  |
| `time_` | int(4) | بله | — |  |
| `Pavarez` | decimal(9) | بله | — |  |
| `Avarez` | money(8) | بله | — |  |
| `Ptax` | decimal(9) | بله | — |  |
| `Tax` | money(8) | بله | — |  |
| `PerPromotion` | decimal(9) | بله | — |  |

### `subtaraz` — 0 ردیف (تخمین)
- کلیدها: PK_subtaraz(PK)=shfacfo, shka
- FK `FK_subtaraz_taraz`: sh_taraz → taraz.sh_taraz

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `sh_taraz` | int(4) | بله | — |  |
| `shfacfo` | int(4) | خیر | — |  |
| `shka` | int(4) | خیر | — |  |
| `naka` | varchar(50) | بله | — |  |
| `tedvah` | decimal(9) | بله | — |  |
| `tedjoz` | int(4) | بله | — |  |
| `kol` | decimal(9) | بله | — |  |
| `vazn` | decimal(9) | بله | — |  |
| `tedbastebandi` | decimal(9) | بله | — |  |

### `sys_anb` — 1 ردیف (تخمین)
- کلیدها: PK_sys_anb(PK)=SysID, shanb
- FK `FK_sys_anb_anbars`: shanb → anbars.rdf_anbar
- FK `FK_sys_anb_osystems`: SysID → osystems.rdf_system
- FK `FK_sys_anb_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `SysID` | int(4) | خیر | — |  |
| `shanb` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `sys_cus` — 2723 ردیف (تخمین)
- کلیدها: PK_sys_cus(PK)=SysID, Shmo
- FK `FK_sys_cus_CUSTOMERS`: Shmo → CUSTOMERS.SHMO
- FK `FK_sys_cus_osystems`: SysID → osystems.rdf_system
- FK `FK_sys_cus_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `SysID` | int(4) | خیر | — |  |
| `Shmo` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `sys_kal` — 1803 ردیف (تخمین)
- کلیدها: PK_sys_kal(PK)=sysid, shka, rdf
- FK `FK_sys_kal_osystems`: sysid → osystems.rdf_system
- FK `FK_sys_kal_sys_kal`: shka → inventory.shka
- FK `FK_sys_kal_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `sysid` | int(4) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `sys_tafsil` — 2690 ردیف (تخمین)
- کلیدها: PK_sys_tafsil(PK)=TafsilId, SysId
- FK `FK_sys_tafsil_osystems`: SysId → osystems.rdf_system
- FK `FK_sys_tafsil_sys_users`: UserId → sys_users.user_id
- FK `FK_sys_tafsil_Tafsil`: TafsilId → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `Rdf` | int(4) | خیر | بله |  |
| `TafsilId` | bigint(8) | خیر | — |  |
| `SysId` | int(4) | خیر | — |  |
| `UserId` | int(4) | خیر | — |  |

### `sys_use` — 6 ردیف (تخمین)
- کلیدها: PK_sys_use(PK)=SysID, shuse
- FK `FK_sys_use_osystems`: SysID → osystems.rdf_system
- FK `FK_sys_use_sys_users`: shuse → sys_users.user_id
- FK `FK_sys_use_sys_users1`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `SysID` | int(4) | خیر | — |  |
| `shuse` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `sys_users` — 6 ردیف (تخمین)
- کلیدها: PK_sys_users(PK)=user_id
- FK `FK_sys_users_CUSTOMERS`: shmo → CUSTOMERS.SHMO
- FK `FK_sys_users_Roles`: role_id → Roles.id
- FK `FK_sys_users_Tafsil`: TafsilID → Tafsil.TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `user_id` | int(4) | خیر | بله |  |
| `user_password` | varbinary(50) | خیر | — |  |
| `user_name` | varchar(80) | بله | — |  |
| `user_lname` | varchar(30) | خیر | — |  |
| `user_fname` | varchar(30) | خیر | — |  |
| `role_id` | int(4) | بله | — |  |
| `skin_id` | int(4) | بله | — |  |
| `user_pic` | image(16) | بله | — |  |
| `IsLocked` | bit(1) | بله | — |  |
| `IsLoggedIn` | bit(1) | بله | — | ((0)) |
| `TafsilID` | bigint(8) | بله | — |  |
| `phone` | nvarchar(22) | بله | — |  |
| `email` | nvarchar(100) | بله | — |  |
| `nationalCode` | nvarchar(22) | بله | — |  |
| `address` | nvarchar(-1) | بله | — |  |
| `active` | bit(1) | بله | — |  |
| `shmo` | int(4) | خیر | — | ((1)) |
| `BackGroundAddress` | nvarchar(4000) | بله | — |  |
| `IsNotePade` | bit(1) | بله | — |  |
| `SysuserTransferCode` | nvarchar(100) | بله | — |  |
| `MoeinId` | bigint(8) | بله | — |  |
| `AccessToCRM` | bit(1) | خیر | — | ((0)) |

### `sys_vis` — 10 ردیف (تخمین)
- کلیدها: PK_sys_vis(PK)=SysID, shvis
- FK `FK_sys_vis_osystems`: SysID → osystems.rdf_system
- FK `FK_sys_vis_sys_users`: UserID → sys_users.user_id
- FK `FK_sys_vis_visitors`: shvis → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `SysID` | int(4) | خیر | — |  |
| `shvis` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `sys_wor` — 0 ردیف (تخمین)
- کلیدها: PK_sys_wor(PK)=SysID, shwor
- FK `FK_sys_wor_osystems`: SysID → osystems.rdf_system
- FK `FK_sys_wor_sys_users`: UserID → sys_users.user_id
- FK `FK_sys_wor_worker`: shwor → worker.worker_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `SysID` | int(4) | خیر | — |  |
| `shwor` | int(4) | خیر | — |  |
| `UserID` | int(4) | خیر | — |  |

### `sysdiagrams` — 9 ردیف (تخمین)
- کلیدها: <text 30>(PK)=diagram_id; UK_principal_name(UQ)=principal_id, name

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `name` | sysname(256) | خیر | — |  |
| `principal_id` | int(4) | خیر | — |  |
| `diagram_id` | int(4) | خیر | بله |  |
| `version` | int(4) | بله | — |  |
| `definition` | varbinary(-1) | بله | — |  |

### `systems` — 2 ردیف (تخمین)
- کلیدها: PK_systems(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `name` | varchar(50) | بله | — |  |
| `active` | int(4) | خیر | — |  |

### `tafif` — 18 ردیف (تخمین)
- کلیدها: pk_tafif(PK)=rdf, p
- FK `FK_tafif_sys_users`: UserID → sys_users.user_id
- FK `FK_tafif_TafifFlag`: flag → TafifFlag.Id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | bigint(8) | خیر | — |  |
| `p` | int(4) | خیر | — |  |
| `mab` | money(8) | خیر | — |  |
| `date` | char(10) | خیر | — |  |
| `dis` | varchar(500) | بله | — |  |
| `shmo` | int(4) | خیر | — |  |
| `done_date` | char(10) | بله | — |  |
| `flag` | int(4) | خیر | — |  |
| `mod` | int(4) | بله | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `GhabzSanadNumber` | int(4) | بله | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `UserID` | int(4) | خیر | — |  |
| `Hour` | char(8) | بله | — |  |

### `tafif_f_formula` — 0 ردیف (تخمین)
- کلیدها: PK_tafif_f_formula(PK)=rdf
- FK `FK_tafif_f_formula_custgroup`: CustGroupRdf → custgroup.group_rdf
- FK `FK_tafif_f_formula_osystems`: LineRdf → osystems.rdf_system

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `az_mab` | decimal(9) | خیر | — |  |
| `to_mab` | decimal(9) | خیر | — |  |
| `tafif_naghdy` | decimal(9) | خیر | — | ((0)) |
| `eteb_time` | int(4) | خیر | — | ((0)) |
| `tafif_hajm` | decimal(9) | خیر | — | ((0)) |
| `tafif_chk_naghdy` | decimal(9) | خیر | — | ((0)) |
| `VisitorTafif` | decimal(9) | خیر | — | ((0)) |
| `PerPos` | decimal(9) | خیر | — | ((0)) |
| `CustGroupRdf` | int(4) | بله | — |  |
| `LineRdf` | int(4) | بله | — |  |

### `tah_sar_barg` — 2 ردیف (تخمین)
- کلیدها: PK_tah_sar_barg(PK)=rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `f_line` | varchar(100) | خیر | — |  |
| `sar_tah` | int(4) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `mohtava` | varchar(1000) | خیر | — | ('ذکر نشده') |

### `taraz` — 0 ردیف (تخمین)
- کلیدها: PK_taraz(PK)=sh_taraz
- FK `FK_taraz_cars`: car_rdf → cars.rdf_car
- FK `FK_taraz_sys_users`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `sh_taraz` | int(4) | خیر | بله |  |
| `date__` | char(10) | خیر | — |  |
| `cars_name` | varchar(70) | خیر | — |  |
| `car_rdf` | int(4) | خیر | — |  |
| `mamorp_name` | varchar(70) | خیر | — |  |
| `mamorp_rdf` | int(4) | خیر | — |  |
| `driver_name` | varchar(70) | خیر | — |  |
| `driver_rdf` | int(4) | خیر | — |  |
| `sharh` | nvarchar(-1) | خیر | — |  |
| `dest` | varchar(150) | خیر | — |  |
| `jam_vah` | decimal(9) | بله | — |  |
| `jam_joz` | int(4) | بله | — |  |
| `jam_kol` | decimal(9) | بله | — |  |
| `vazn_kol` | decimal(9) | بله | — |  |
| `toz_fact` | text(16) | بله | — |  |
| `chap` | int(4) | بله | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `UserID` | int(4) | خیر | — |  |
| `StartDateNet` | char(10) | بله | — |  |
| `EndDateNet` | char(10) | بله | — |  |
| `StartTimeNet` | char(8) | بله | — |  |
| `EndTimeNet` | char(8) | بله | — |  |
| `StartLatNet` | float(8) | بله | — |  |
| `StartLngNet` | float(8) | بله | — |  |
| `EndLatNet` | float(8) | بله | — |  |
| `EndLngNet` | float(8) | بله | — |  |

### `taraz_pish` — 0 ردیف (تخمین)
- کلیدها: PK_taraz_pish(PK)=sh_taraz_pish
- FK `FK_taraz_pish_sys_users`: UserID → sys_users.user_id
- FK `FK_taraz_pish_sys_users1`: UserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `sh_taraz_pish` | int(4) | خیر | بله |  |
| `date__` | char(10) | خیر | — |  |
| `cars_name` | varchar(70) | خیر | — |  |
| `car_rdf` | int(4) | خیر | — |  |
| `mamorp_name` | varchar(70) | خیر | — |  |
| `mamorp_rdf` | int(4) | خیر | — |  |
| `driver_name` | varchar(70) | خیر | — |  |
| `driver_rdf` | int(4) | خیر | — |  |
| `sharh` | varchar(-1) | خیر | — |  |
| `dest` | varchar(150) | خیر | — |  |
| `jam_vah` | decimal(9) | بله | — |  |
| `jam_joz` | int(4) | بله | — |  |
| `jam_kol` | decimal(9) | بله | — |  |
| `vazn_kol` | decimal(9) | بله | — |  |
| `toz_fact` | text(16) | بله | — |  |
| `chap` | int(4) | بله | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `UserID` | int(4) | خیر | — |  |

### `task` — 0 ردیف (تخمین)
- کلیدها: PK_task(PK)=taskID
- FK `FK_task_convention`: servedConventionID → convention.conventionID
- FK `FK_task_CUSTOMERS`: CustomerID → CUSTOMERS.SHMO
- FK `FK_task_Phase`: phaseID → Phase.PhaseID
- FK `FK_task_project`: projectID → project.ProjectID
- FK `FK_task_taskRelationType`: taskRelationTypeID → taskRelationType.TypeID
- FK `FK_task_taskSubject`: CreationUserID → user.UserID
- FK `FK_task_user`: CurrentUserID → user.UserID
- FK `FK_task_user1`: CurrentUserID → user.UserID
- FK `FK_task_user2`: taskDoneUserID → user.UserID
- FK `FK_task_user3`: CurrentUserID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `taskID` | int(4) | خیر | بله |  |
| `CustomerID` | int(4) | بله | — |  |
| `taskRelationTypeID` | int(4) | بله | — |  |
| `taskSubjectID` | int(4) | بله | — |  |
| `taskDoneUserID` | int(4) | بله | — |  |
| `CreationUserID` | int(4) | بله | — |  |
| `taskAttachmentID` | int(4) | بله | — |  |
| `StatusID` | int(4) | بله | — |  |
| `taskCreationDate` | nvarchar(30) | بله | — |  |
| `taskCreationTime` | nvarchar(30) | بله | — |  |
| `taskDoneDate` | nvarchar(30) | بله | — |  |
| `taskDoneTime` | nvarchar(30) | بله | — |  |
| `DueDate` | nvarchar(30) | بله | — |  |
| `DueTime` | nvarchar(30) | بله | — |  |
| `CurrentUserID` | int(4) | بله | — |  |
| `departmentID` | int(4) | بله | — |  |
| `projectID` | int(4) | بله | — |  |
| `phaseID` | int(4) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |
| `parentTaskID` | int(4) | بله | — |  |
| `followingNO` | nvarchar(40) | بله | — |  |
| `ReferNO` | nvarchar(100) | بله | — |  |
| `currentDepartmentID` | int(4) | بله | — |  |
| `PriorityID` | int(4) | بله | — |  |
| `servedConventionID` | int(4) | بله | — |  |
| `donDate` | nvarchar(30) | بله | — |  |
| `donTime` | nvarchar(30) | بله | — |  |

### `taskAttachment` — 0 ردیف (تخمین)
- کلیدها: PK_taskAttachment(PK)=attachmentID
- FK `FK_taskAttachment_task`: taskID → task.taskID
- FK `FK_taskAttachment_user`: userID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `attachmentID` | int(4) | خیر | بله |  |
| `taskID` | int(4) | بله | — |  |
| `userID` | int(4) | بله | — |  |
| `date` | nvarchar(30) | بله | — |  |
| `time` | nvarchar(30) | بله | — |  |
| `attachmentName` | nvarchar(600) | بله | — |  |
| `AttachmentAddress` | nvarchar(600) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `taskComment` — 0 ردیف (تخمین)
- کلیدها: PK_taskDescription(PK)=commentID
- FK `FK_taskComment_task`: taskID → task.taskID
- FK `FK_taskComment_user`: UserID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `commentID` | int(4) | خیر | بله |  |
| `taskID` | int(4) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `date` | nvarchar(30) | بله | — |  |
| `time` | nvarchar(30) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `taskLog` — 0 ردیف (تخمین)
- کلیدها: PK_taskLog(PK)=editTaskID
- FK `FK_taskLog_CUSTOMERS`: fromCustomerID → CUSTOMERS.SHMO
- FK `FK_taskLog_CUSTOMERS1`: toCustomerID → CUSTOMERS.SHMO
- FK `FK_taskLog_task`: taskID → task.taskID
- FK `FK_taskLog_taskLog`: fromPriorityID → taskPriority.priorityID
- FK `FK_taskLog_taskPriority`: toPriorityID → taskPriority.priorityID
- FK `FK_taskLog_taskRelationType`: fromRelationTypeID → taskRelationType.TypeID
- FK `FK_taskLog_taskRelationType1`: toRelationTypeID → taskRelationType.TypeID
- FK `FK_taskLog_taskSubject`: fromSubjectID → taskSubject.subjectID
- FK `FK_taskLog_taskSubject1`: toSubjectID → taskSubject.subjectID
- FK `FK_taskLog_user`: regUserID → user.UserID
- FK `FK_taskLog_user1`: fromResponsibleUserID → user.UserID
- FK `FK_taskLog_user2`: toResponsibleUserID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `editTaskID` | int(4) | خیر | بله |  |
| `taskID` | int(4) | بله | — |  |
| `fromCustomerID` | int(4) | بله | — |  |
| `toCustomerID` | int(4) | بله | — |  |
| `fromRelationTypeID` | int(4) | بله | — |  |
| `toRelationTypeID` | int(4) | بله | — |  |
| `fromPriorityID` | int(4) | بله | — |  |
| `toPriorityID` | int(4) | بله | — |  |
| `fromReferingNO` | nvarchar(100) | بله | — |  |
| `toReferingNO` | nvarchar(100) | بله | — |  |
| `fromDueDate` | nvarchar(20) | بله | — |  |
| `toDueDate` | nvarchar(20) | بله | — |  |
| `fromCommnt` | nvarchar(-1) | بله | — |  |
| `toComment` | nvarchar(-1) | بله | — |  |
| `regUserID` | int(4) | بله | — |  |
| `fromResponsibleUserID` | int(4) | بله | — |  |
| `toResponsibleUserID` | int(4) | بله | — |  |
| `fromSubjectID` | int(4) | بله | — |  |
| `toSubjectID` | int(4) | بله | — |  |

### `taskPriority` — 0 ردیف (تخمین)
- کلیدها: PK_taskPriority(PK)=priorityID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `priorityID` | int(4) | خیر | بله |  |
| `priorityName` | nvarchar(200) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `taskRefer` — 0 ردیف (تخمین)
- کلیدها: PK_Erja(PK)=referID
- FK `FK_taskRefer_task`: taskID → task.taskID
- FK `FK_taskRefer_user`: userID → user.UserID
- FK `FK_taskRefer_user1`: toUserID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `referID` | int(4) | خیر | بله |  |
| `taskID` | int(4) | بله | — |  |
| `userID` | int(4) | بله | — |  |
| `toUserID` | int(4) | بله | — |  |
| `date` | nvarchar(30) | بله | — |  |
| `time` | nvarchar(30) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `taskRelationType` — 0 ردیف (تخمین)
- کلیدها: PK_taskType(PK)=TypeID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `TypeID` | int(4) | خیر | بله |  |
| `TypeName` | nvarchar(100) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `taskStatus` — 0 ردیف (تخمین)
- کلیدها: PK_taskStatus(PK)=StatusID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `StatusID` | int(4) | خیر | بله |  |
| `StatusName` | nvarchar(100) | بله | — |  |

### `taskSubject` — 0 ردیف (تخمین)
- کلیدها: PK_taskSubject(PK)=subjectID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `subjectID` | int(4) | خیر | بله |  |
| `subjectName` | nvarchar(200) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `taskTimeConsumption` — 0 ردیف (تخمین)
- کلیدها: PK_taskTimeConsumption(PK)=timeConsumptionID
- FK `FK_taskTimeConsumption_task`: taskID → task.taskID
- FK `FK_taskTimeConsumption_user`: userID → user.UserID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `timeConsumptionID` | int(4) | خیر | بله |  |
| `taskID` | int(4) | بله | — |  |
| `userID` | int(4) | بله | — |  |
| `fromDate` | nvarchar(30) | بله | — |  |
| `fromTime` | nvarchar(30) | بله | — |  |
| `toDate` | nvarchar(30) | بله | — |  |
| `toTime` | nvarchar(30) | بله | — |  |
| `comment` | nvarchar(-1) | بله | — |  |

### `tblSefaresh` — 0 ردیف (تخمین)
- کلیدها: PK_tblSefaresh(PK)=RowID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `RowID` | int(4) | خیر | بله |  |
| `Explain` | nvarchar(4000) | خیر | — |  |

### `tblmoghayerat` — 0 ردیف (تخمین)
- کلیدها: PK_tblmoghayerat(PK)=ID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `ID` | int(4) | خیر | بله |  |
| `RDF` | int(4) | خیر | — |  |
| `SHMO` | bigint(8) | خیر | — |  |
| `Date_` | varchar(10) | خیر | — |  |
| `TypeID` | int(4) | خیر | — |  |
| `TypeName` | varchar(50) | خیر | — |  |

### `tempForAccountingMapping` — 37 ردیف (تخمین)
- کلیدها: PK_tempForAccountingMapping(PK)=MappingID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MappingID` | int(4) | خیر | — |  |
| `AtiranName` | nvarchar(2000) | بله | — |  |
| `KolID` | int(4) | بله | — |  |
| `MoeinID` | int(4) | بله | — |  |
| `TafsilID` | int(4) | بله | — |  |
| `IsEdit` | bit(1) | بله | — |  |
| `Level` | tinyint(1) | بله | — |  |

### `tempForGrouh` — 10 ردیف (تخمین)
- کلیدها: PK_grouh_temp(PK)=GrouhID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GrouhID` | bigint(8) | خیر | — |  |
| `GrouhCode` | nchar(4) | خیر | — |  |
| `Name` | nvarchar(2000) | خیر | — |  |
| `Daraie_Bedehi` | int(4) | بله | — |  |
| `IsEdit` | bit(1) | خیر | — | ((0)) |

### `tempForKol` — 30 ردیف (تخمین)
- کلیدها: PK_Kol_temp(PK)=KolID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `KolID` | bigint(8) | خیر | — |  |
| `Name` | nvarchar(2000) | بله | — |  |
| `KolCode` | nchar(6) | بله | — |  |
| `GrouhID` | bigint(8) | بله | — |  |
| `CanDefineMoein` | bit(1) | بله | — |  |
| `IsEdit` | bit(1) | بله | — | ((0)) |

### `tempForMoein` — 18 ردیف (تخمین)
- کلیدها: PK_Moein_temp(PK)=MoeinID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `MoeinID` | bigint(8) | خیر | — |  |
| `Name` | nvarchar(2000) | بله | — |  |
| `MoeinCode` | nchar(6) | بله | — |  |
| `KolID` | bigint(8) | بله | — |  |
| `FullName` | nvarchar(2000) | بله | — |  |
| `FullCode` | nchar(16) | بله | — |  |
| `IsEdit` | bit(1) | بله | — | ((0)) |
| `AddToDoc` | bit(1) | بله | — |  |

### `tempForTafsil` — 0 ردیف (تخمین)
- کلیدها: PK_Tafsil_temp(PK)=TafsilID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `TafsilID` | bigint(8) | خیر | — |  |
| `Name` | nvarchar(2000) | خیر | — |  |
| `TafsilCode` | nchar(10) | خیر | — |  |
| `MoeinID` | bigint(8) | خیر | — |  |
| `FullName` | nvarchar(2000) | خیر | — |  |
| `FullCode` | nchar(26) | خیر | — |  |
| `AddToDoc` | bit(1) | بله | — |  |
| `IsEdit` | bit(1) | خیر | — | ((0)) |

### `tempForTafsilGroup` — 0 ردیف (تخمین)
- کلیدها: PK_tempForTafsilGroup(PK)=GroupId

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `GroupId` | bigint(8) | خیر | — |  |
| `GroupName` | nvarchar(1000) | خیر | — |  |
| `Code` | nchar(6) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |

### `template_for_putchk` — 0 ردیف (تخمین)
- کلیدها: PK_template_for_putchk(PK)=shputchk

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `putdate` | char(10) | خیر | — |  |
| `sardate` | char(10) | بله | — |  |
| `shputchk` | varchar(30) | خیر | — |  |
| `bankrdf` | int(4) | خیر | — |  |
| `putchkmab` | money(8) | خیر | — |  |
| `shmo` | bigint(8) | خیر | — |  |
| `putchk_dis` | varchar(500) | خیر | — |  |
| `putchk_satus` | int(4) | خیر | — |  |
| `shfacbuy` | bigint(8) | خیر | — | (0) |
| `girande` | varchar(500) | خیر | — | ('ذکر نشده') |
| `ghno` | bigint(8) | خیر | — | (0) |
| `rdf_in_ghno` | int(4) | خیر | — | (4) |
| `done_date` | char(10) | بله | — |  |
| `mod` | int(4) | بله | — |  |
| `new` | int(4) | بله | — |  |
| `amani` | char(1) | بله | — |  |
| `chkbatch_num` | int(4) | بله | — |  |
| `babat` | varchar(1000) | بله | — |  |
| `sysid` | int(4) | بله | — | ((1)) |
| `UserID` | int(4) | بله | — |  |
| `IdDocumentZirSarfasl` | int(4) | بله | — |  |

### `user` — 1 ردیف (تخمین)
- کلیدها: PK_Users(PK)=UserID
- FK `FK_user_sys_users`: AtiranUserID → sys_users.user_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `UserID` | int(4) | خیر | بله |  |
| `AtiranUserID` | int(4) | بله | — |  |
| `Username` | nvarchar(1000) | بله | — |  |
| `Tell` | nvarchar(22) | بله | — |  |
| `Cellphone` | nvarchar(22) | بله | — |  |
| `Address` | nvarchar(-1) | بله | — |  |
| `NationalCode` | nvarchar(22) | بله | — |  |
| `Email` | nvarchar(100) | بله | — |  |
| `PersonalID` | int(4) | بله | — | ((0)) |

### `userRole` — 1 ردیف (تخمین)
- کلیدها: PK_UserRolls_1(PK)=UserRollID
- FK `FK_userRole_companyTask`: CompanyID → companyTask.CompanyID
- FK `FK_userRole_department`: DepartmentID → department.DepartmentID
- FK `FK_userRole_project`: ProjectID → project.ProjectID
- FK `FK_userRole_user`: UserID → user.UserID
- FK `FK_UserRolls_UserRolls`: PhaseID → Phase.PhaseID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `UserRollID` | int(4) | خیر | بله |  |
| `UserID` | int(4) | بله | — |  |
| `CompanyID` | int(4) | بله | — |  |
| `DepartmentID` | int(4) | بله | — |  |
| `ProjectID` | int(4) | بله | — |  |
| `PhaseID` | int(4) | بله | — |  |
| `RollID` | int(4) | بله | — |  |
| `Active` | bit(1) | بله | — |  |

### `vis_goals` — 4 ردیف (تخمین)
- کلیدها: PK_vis_goals_1(PK)=rdf
- FK `FK_vis_goals_baze`: baze_rdf → baze.rdf
- FK `FK_vis_goals_custgroup`: CustomerGroupRdf → custgroup.group_rdf
- FK `FK_vis_goals_inventory`: InventoryID → inventory.shka
- FK `FK_vis_goals_kagroup`: KalaGroupRdf → kagroup.group_rdf
- FK `FK_vis_goals_osystems`: SysID → osystems.rdf_system
- FK `FK_vis_goals_visitors`: vis_rdf → visitors.vis_rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `baze_rdf` | int(4) | خیر | — |  |
| `vis_rdf` | int(4) | خیر | — |  |
| `CustomerGroupRdf` | int(4) | بله | — |  |
| `mab` | money(8) | خیر | — |  |
| `ted` | decimal(9) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `KalaGroupRdf` | int(4) | بله | — |  |
| `InventoryID` | bigint(8) | بله | — |  |
| `ProvinceID` | int(4) | خیر | — | ((0)) |
| `CityID` | int(4) | خیر | — | ((0)) |
| `RegionID` | int(4) | خیر | — | ((0)) |
| `PathID` | int(4) | خیر | — | ((0)) |
| `SysID` | int(4) | بله | — |  |
| `QuarterID` | int(4) | خیر | — | ((0)) |

### `visitors` — 10 ردیف (تخمین)
- کلیدها: pk_visitors(PK)=vis_rdf
- FK `<text 30>`: rdf_device_distribution → DeviceDistribution.ID
- FK `FK_visitors_Devices`: rdf_device → Device.DeviceID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `vis_rdf` | int(4) | خیر | بله |  |
| `vis_name` | varchar(30) | خیر | — |  |
| `vis_addre` | varchar(80) | خیر | — | ('ذکرنشده') |
| `vis_tell1` | varchar(25) | خیر | — | ('ذکرنشده') |
| `vis_tell2` | varchar(25) | خیر | — | ('ذکرنشده') |
| `vis_cell` | varchar(25) | خیر | — | ('ذکرنشده') |
| `vis_man` | money(8) | خیر | — |  |
| `VIs_region` | int(4) | خیر | — |  |
| `viss_date` | char(10) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `vis_city` | int(4) | بله | — |  |
| `h_sabet` | money(8) | بله | — |  |
| `image` | image(16) | بله | — |  |
| `is_supervisor` | char(1) | بله | — |  |
| `supervisor_rdf` | int(4) | بله | — |  |
| `supervisor_per` | decimal(9) | بله | — |  |
| `dar_z` | decimal(9) | بله | — |  |
| `eteb` | money(8) | بله | — |  |
| `per_p_d_naghd` | decimal(9) | بله | — |  |
| `per_p_d_check` | decimal(9) | بله | — |  |
| `per_jar_bch` | decimal(9) | بله | — |  |
| `kind` | int(4) | بله | — |  |
| `tedad_fmmt` | int(4) | بله | — |  |
| `mab_fmmt` | money(8) | بله | — |  |
| `rdf_device` | int(4) | بله | — |  |
| `TedadFactorMojazMande` | int(4) | خیر | — | ((0)) |
| `<text 30>` | decimal(9) | خیر | — | ((0)) |
| `Type1` | bit(1) | بله | — |  |
| `Type2` | bit(1) | بله | — |  |
| `Username` | nvarchar(200) | بله | — |  |
| `Password` | nvarchar(200) | بله | — |  |
| `UserID` | int(4) | بله | — |  |
| `rdf_device_distribution` | int(4) | بله | — |  |
| `NotCalculateCommision` | bit(1) | خیر | — | ((0)) |
| `CountCustomerBed` | int(4) | خیر | — | ((0)) |
| `BlackList` | bit(1) | خیر | — | ((0)) |

### `worker` — 0 ردیف (تخمین)
- کلیدها: PK_worker(PK)=worker_id

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `worker_id` | int(4) | خیر | بله |  |
| `worker_name` | varchar(40) | خیر | — |  |
| `worker_addre` | varchar(80) | خیر | — |  |
| `worker_tell1` | varchar(25) | خیر | — |  |
| `worker_tell2` | varchar(25) | خیر | — |  |
| `worker_cell` | varchar(25) | خیر | — |  |
| `worker_man` | money(8) | خیر | — |  |
| `workers_date` | char(10) | خیر | — |  |
| `active` | char(1) | خیر | — |  |
| `h_sabet` | money(8) | خیر | — |  |
| `image` | image(16) | بله | — |  |
| `semat` | varchar(40) | خیر | — |  |
| `shrhe_amal` | varchar(200) | خیر | — |  |
| `mahale_amal` | varchar(80) | خیر | — |  |

### `zir_sanadenteghal` — 0 ردیف (تخمین)
- کلیدها: PK_zir_sanadenteghal_ID(PK)=ID
- FK `<text 30>`: shka → inventory.shka
- FK `<text 37>`: ProductionSeriesId → ProductionSeries.ID
- FK `<text 35>`: sh_sanad → sanad_enteghal.sh_sanad
- FK `<text 35>`: Rdf_ → sanad_enteghal.Rdf_
- FK `FK_zir_sanadenteghal_Variety`: VarietyID → Variety.VarietyID

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `sh_sanad` | int(4) | خیر | — |  |
| `shka` | bigint(8) | خیر | — |  |
| `anb_s` | int(4) | خیر | — |  |
| `anb_d` | int(4) | خیر | — |  |
| `tedv` | decimal(9) | خیر | — |  |
| `tedj` | int(4) | خیر | — |  |
| `tedb` | decimal(9) | خیر | — |  |
| `na_anb1` | varchar(30) | خیر | — |  |
| `na_anb2` | varchar(30) | خیر | — |  |
| `Active` | bit(1) | خیر | — | ((1)) |
| `ID` | bigint(8) | خیر | بله |  |
| `Rdf_` | bigint(8) | خیر | — | ((1)) |
| `VarietyID` | int(4) | بله | — |  |
| `ProductionSeriesId` | bigint(8) | بله | — |  |

### `zirsarfasls` — 37 ردیف (تخمین)
- کلیدها: PK_zirsarfasls(PK)=rdf
- FK `FK_zirsarfasls_sarfasls`: rdf_sarfasl → sarfasls.rdf

| ستون | نوع | Null | Identity | پیش‌فرض |
|---|---|---|---|---|
| `rdf` | int(4) | خیر | بله |  |
| `radif` | int(4) | بله | — |  |
| `rdf_sarfasl` | int(4) | خیر | — |  |
| `name` | nvarchar(-1) | خیر | — |  |
| `man` | money(8) | خیر | — |  |
| `Active` | bit(1) | بله | — |  |

## ۴) قالب ذخیره تاریخ (Probe)

| ستون | نوع | ماهیت | بازه واقعی | نشانه‌ها |
|---|---|---|---|---|
| `BANK.START_DATE` | char | text | 1405/06/10 تا 1405/06/10 | jalali_like=8 iso_like=0 |
| `CUSTOMERS.DateOfBirth` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `CUSTOMERS.date` | char | text | 1405/06/10 تا 2026-09-01 | jalali_like=2723 iso_like=1 |
| `CUSTOMERS.maxopen_time` | char | text | 1499/12/29 تا 1499/12/29 | jalali_like=2724 iso_like=0 |
| `Sys_Mandeh_Customer.MaxOpenTime` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `ZirSarFaslDocuments.date` | nvarchar | text | 1405/06/03 تا 1405/07/11 | jalali_like=231 iso_like=0 |
| `ZirSarFaslDocuments.doneDate` | nvarchar | text | 1405/06/11 تا 1405/07/11 | jalali_like=231 iso_like=0 |
| `act_zirsarfasls.DoneDate` | nvarchar | text | 1405/06/10 تا 2026-09-01 | jalali_like=290 iso_like=1 |
| `act_zirsarfasls.TimeServer` | nvarchar | text | 08:18:00 تا 15:52:13 | jalali_like=0 iso_like=0 |
| `act_zirsarfasls.date` | char | text | 1405/06/03 تا 1405/07/11 | jalali_like=291 iso_like=0 |
| `back_sanad.DateSendToSystemTax` | nchar | text | None تا None | jalali_like=0 iso_like=0 |
| `back_sanad.DeleteDate` | nvarchar | text | 1405/06/28 تا 1405/06/28 | jalali_like=1 iso_like=0 |
| `back_sanad.DoneDate` | nvarchar | text | 1405/06/11 تا 1405/07/11 | jalali_like=77 iso_like=0 |
| `back_sanad.TImeSendToSystemTax` | nchar | text | None تا None | jalali_like=0 iso_like=0 |
| `back_sanad.TaeedDate` | char | text | None تا None | jalali_like=0 iso_like=0 |
| `back_sanad.TaeedWarehousDate` | char | text | None تا None | jalali_like=0 iso_like=0 |
| `back_sanad.TaxManualDate` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `back_sanad.UIdTax` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `back_sanad.date_` | char | text | 1405/06/11 تا 1405/07/11 | jalali_like=77 iso_like=0 |
| `ban_act.Time` | nvarchar | text | 07:23:01 تا 18:39:08 | jalali_like=0 iso_like=0 |
| `ban_act.act_date` | char | text | 1405/06/04 تا 1405/07/11 | jalali_like=1232 iso_like=0 |
| `buyfact.DATE` | char | text | 1405/06/10 تا 1405/07/11 | jalali_like=62 iso_like=0 |
| `buyfact.DateModatPardakht` | nvarchar | text | 1405/06/11 تا 1405/07/11 | jalali_like=62 iso_like=0 |
| `buyfact.SystemRegistrationDate` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `buyfact.SystemSettlementDate` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `buyfact.TaeedDate` | char | text | None تا None | jalali_like=0 iso_like=0 |
| `buyfact.done_date` | char | text | 1405/06/11 تا 1405/07/11 | jalali_like=62 iso_like=0 |
| `cust_act.date` | char | text | 1405/06/03 تا 2026-09-01 | jalali_like=6175 iso_like=1 |
| `cust_act.done_date` | char | text | 1405/06/10 تا 1405/07/11 | jalali_like=6176 iso_like=0 |
| `cust_act.t_time` | datetime | datetime | 2026-09-01 10:48:55 تا 2026-10-03 15:06:03 |  |
| `dar.ClockTime` | nvarchar | text | 07:20:06.5111508 تا 18:38:35.0015012 | jalali_like=0 iso_like=0 |
| `dar.TaeedDateServer` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `dar.TaeedTimeServer` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `dar.date` | char | text | 1405/06/01 تا 1405/07/11 | jalali_like=1063 iso_like=0 |
| `dar.done_date` | char | text | 1405/06/10 تا 1405/07/11 | jalali_like=1063 iso_like=0 |
| `getchk.DateOfReceipt` | char | text | None تا None | jalali_like=0 iso_like=0 |
| `getchk.done_date` | char | text | 1405/06/10 تا 1405/07/11 | jalali_like=118 iso_like=0 |
| `getchk.getdate` | char | text | 1402/11/30 تا 1405/07/11 | jalali_like=118 iso_like=0 |
| `getchk.kharj_date` | char | text | -- تا ذكر نشده | jalali_like=95 iso_like=0 |
| `getchk.kharj_done_date` | char | text | -- تا ذكر نشده | jalali_like=95 iso_like=0 |
| `getchk.naghddate` | char | text | -- تا ذكر نشده | jalali_like=0 iso_like=0 |
| `getchk.naghddonedate` | char | text | -- تا ذكر نشده | jalali_like=0 iso_like=0 |
| `getchk.our_bankrdf_date` | char | text | -- تا ذكر نشده | jalali_like=0 iso_like=0 |
| `getchk.our_bankrdf_donedate` | char | text | -- تا 1405/06/10 | jalali_like=18 iso_like=0 |
| `getchk.sardate` | char | text | 1402/12/10 تا 1405/09/12 | jalali_like=118 iso_like=0 |
| `inventory.ExpirationDate` | nchar | text |  تا  | jalali_like=0 iso_like=0 |
| `ka_act.act_date` | char | text | 1405/03/22 تا 1405/07/11 | jalali_like=6359 iso_like=0 |
| `ka_act.done_date` | char | text | 1405/06/10 تا 1405/07/11 | jalali_like=6359 iso_like=0 |
| `kagroup.stdate` | datetime | datetime | 1900-01-01 00:00:00 تا 2026-09-06 12:54:30 |  |
| `meelano_prefactors.delivery_date` | nvarchar | text |  تا  | jalali_like=0 iso_like=0 |
| `meelano_prefactors.updated_at` | datetime2 | datetime | 2026-09-27 13:09:56 تا 2026-09-30 09:10:15 |  |
| `putchk.DateOfReceipt` | char | text | 1405/06/14 تا 1405/06/14 | jalali_like=1 iso_like=0 |
| `putchk.done_date` | char | text | 1405/06/10 تا 1405/07/01 | jalali_like=202 iso_like=0 |
| `putchk.putdate` | char | text | 00/00/00 تا 1405/07/01 | jalali_like=65 iso_like=0 |
| `putchk.sardate` | char | text | 1402/03/18 تا 99/12/29 | jalali_like=65 iso_like=0 |
| `sailfact.DateSendToTaxSystem` | nchar | text | None تا None | jalali_like=0 iso_like=0 |
| `sailfact.TaeedDate` | nvarchar | text | 1405/06/10 تا 1405/07/11 | jalali_like=1168 iso_like=0 |
| `sailfact.TaeedWarehousDate` | char | text | None تا None | jalali_like=0 iso_like=0 |
| `sailfact.TaxManualDate` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `sailfact.TimeSendToTaxSystem` | nchar | text | None تا None | jalali_like=0 iso_like=0 |
| `sailfact.UidTax` | nvarchar | text | None تا None | jalali_like=0 iso_like=0 |
| `sailfact.date` | char | text | 1405/06/09 تا 1405/07/11 | jalali_like=1168 iso_like=0 |
| `sailfact.done_date` | char | text | 1405/06/10 تا 1405/07/11 | jalali_like=1168 iso_like=0 |
| `sailfact.t_date` | char | text | 1405/06/09 تا 1405/07/11 | jalali_like=1168 iso_like=0 |
| `sailfact.time_` | varchar | text | 07:24 تا 15:42 | jalali_like=0 iso_like=0 |
| `sailfact_pish.DateRecive` | varchar | text | 1405/07/04 تا 1405/07/08 | jalali_like=13 iso_like=0 |
| `sailfact_pish.DateTaedForush` | nvarchar | text | 1405/07/06 تا 1405/07/08 | jalali_like=2 iso_like=0 |
| `sailfact_pish.DateTaedHesabdari` | nvarchar | text | 1405/07/06 تا 1405/07/08 | jalali_like=2 iso_like=0 |
| `sailfact_pish.RejectedDate` | nvarchar | text | 1405/07/07 تا 1405/07/07 | jalali_like=9 iso_like=0 |
| `sailfact_pish.TimeRecive` | varchar | text | 10:13:42 تا 9:9:30 | jalali_like=0 iso_like=0 |
| `sailfact_pish.date` | char | text | 1405/07/06 تا 2026/09/27 | jalali_like=12 iso_like=0 |
| `sailfact_pish.date_f` | char | text | -- تا -- | jalali_like=0 iso_like=0 |
| `sailfact_pish.done_date` | char | text |  تا 1405/07/08 | jalali_like=11 iso_like=0 |
| `visitors.viss_date` | char | text |  تا 1405/06/15 | jalali_like=9 iso_like=0 |

## ۵) دامنه مقادیر وضعیت/کد (Status Domains)

- **BANK.AccountType**: `1`×8
- **BANK.Active**: `True`×7, `False`×1
- **CUSTOMERS.PersonalityType**: `1`×2722, `2`×1, `None`×1
- **CUSTOMERS.TaxInvoiceType**: `0`×2714, `1`×10
- **CUSTOMERS.active**: `t`×2723, `f`×1
- **CUSTOMERS.group_rdf**: `1`×2499, `2`×215, `3`×10
- **CUSTOMERS.hesab_status**: `1`×2724
- **CUSTOMERS.kind**: `1`×2499, `2`×215, `4`×10
- **DeviceSettings.AccessedKalaGroup**: خطا: ("Invalid object name 'dbo.DeviceSettings'.", None)
- **DeviceSettings.IsTaxActive**: خطا: ("Invalid object name 'dbo.DeviceSettings'.", None)
- **DeviceSettings.MojoodiType**: خطا: ("Invalid object name 'dbo.DeviceSettings'.", None)
- **DeviceSettings.PriceGrpType**: خطا: ("Invalid object name 'dbo.DeviceSettings'.", None)
- **Sys_Mandeh_Customer.CreditCheckType**: `0`×2681, `1`×43
- **TellBook.Active**: `True`×2732
- **ZirSarFaslDocuments.Active**: `True`×203, `False`×28
- **ZirSarFaslDocuments.Kind**: `58`×142, `64`×41, `51`×30, `55`×10, `56`×8
- **act_zirsarfasls.kind**: `58`×131, `64`×47, `-1`×36, `51`×31, `55`×19, `56`×9, `2`×6, `89`×5, `6`×3, `1`×3, `0`×1
- **back_sanad.Active**: `True`×76, `False`×1
- **back_sanad.kind**: `6`×47, `2`×23, `1`×4, `5`×3
- **ban_act.isActive**: `None`×1223, `True`×9
- **buyfact.CashType**: `1`×62
- **buyfact.active**: `t`×56, `f`×6
- **chkbatch.Active**: `None`×15
- **chkbatch.type**: `0`×8, `1`×5, `2`×2
- **cust_act.isActive**: `True`×3449, `None`×2723, `False`×4
- **dar.Active**: `True`×938, `False`×125
- **forosh_price.active**: `t`×1803
- **forosh_price.group_rdf**: `1`×1341, `2`×414, `3`×48
- **getchk.CheckTypeID**: `1`×114, `2`×4
- **inventory.ActiveCapillarySales**: `False`×1803
- **inventory.ActiveOnlineSales**: `False`×1803
- **inventory.GoodsKindID**: `1`×1803
- **inventory.InventoryTypeID**: `1`×1755, `2`×47, `3`×1
- **inventory.active**: `t`×1803
- **inventory.group_rdf**: `1`×1341, `2`×414, `3`×48
- **ka_act.active**: `t`×6281, `f`×78
- **kagroup.Active**: `True`×3
- **kagroup.GroupCode**: `None`×3
- **kagroup.GroupLevel**: `None`×2, `0`×1
- **kagroup.ParentGroupRdf**: `None`×3
- **kagroup.group_code**: ``×2, `None`×1
- **kagroup.group_name**: `شكلات و آبنبات ها`×1, `توليد`×1, `--`×1
- **kagroup.group_rdf**: `1`×1, `2`×1, `3`×1
- **meelano_prefactors.invoice_status**: `ready`×10, `not_ready`×2, `None`×1
- **meelano_prefactors.settlement_type**: `اعتباری`×13
- **meelano_prefactors.status**: `sent`×9, `pending_approval`×2, `draft`×2
- **putchk.CheckTypeID**: `1`×202
- **putchk.putchk_status**: `0`×109, `1`×63, `2`×29, `7`×1
- **sailfact.Status**: `1`×1168
- **sailfact.TypeInvoiceSentToMoadiyan**: `None`×1168
- **sailfact.active**: `t`×970, `f`×198
- **sailfact_pish.active**: `t`×11, `f`×2
- **subbuyfact.active**: `t`×181, `f`×30
- **subsailfact.active**: `t`×3871, `f`×1120
- **subsailfact_pish.active**: `t`×14, `f`×1
- **sys_users.active**: `True`×5, `False`×1
- **vis_goals.Active**: `True`×4
- **vis_goals.CustomerGroupRdf**: `None`×4
- **vis_goals.KalaGroupRdf**: `None`×4
- **visitors.Type1**: `False`×9, `None`×1
- **visitors.Type2**: `False`×9, `None`×1
- **visitors.active**: `t`×10
- **visitors.kind**: `1`×10

## ۶) تازگی داده (Freshness)

| جدول | تخمین ردیف | ردیف دقیق |
|---|---|---|
| `_server_now` | None | — |
| `cust_act` | 6176 | 6176 |
| `getchk` | 118 | 118 |
| `putchk` | 202 | 202 |
| `sailfact` | 1168 | 1168 |
| `subsailfact` | 4991 | 4991 |
| `visitors` | 10 | 10 |

## ۷) خطاهای Probe

