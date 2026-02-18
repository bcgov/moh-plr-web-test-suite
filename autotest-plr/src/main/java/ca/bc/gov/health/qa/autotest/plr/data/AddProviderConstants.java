package ca.bc.gov.health.qa.autotest.plr.data;

import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusCodeOption;
import ca.bc.gov.health.qa.autotest.plr.web.tests.model.StatusReasonCodeOption;

import java.util.List;
import java.util.Map;

public class AddProviderConstants {

    private static final List<StatusReasonCodeOption> CANCELLED_REASON_CODE_OPTIONS = List.of(
            StatusReasonCodeOption.AU,
            StatusReasonCodeOption.INNONPRAC,
            StatusReasonCodeOption.LAP,
            StatusReasonCodeOption.DEN,
            StatusReasonCodeOption.MEDSTUD,
            StatusReasonCodeOption.ORG,
            StatusReasonCodeOption.OOP,
            StatusReasonCodeOption.RESDISC,
            StatusReasonCodeOption.RET,
            StatusReasonCodeOption.UNK,
            StatusReasonCodeOption.VW
    );

    private static final List<StatusReasonCodeOption> ACTIVE_REASON_CODE_OPTIONS = List.of(
            StatusReasonCodeOption.ASSOC,
            StatusReasonCodeOption.GS,
            StatusReasonCodeOption.LAP,
            StatusReasonCodeOption.MEDSTUD,
            StatusReasonCodeOption.NONPRAC,
            StatusReasonCodeOption.ORG,
            StatusReasonCodeOption.OOP,
            StatusReasonCodeOption.PRAC,
            StatusReasonCodeOption.RET,
            StatusReasonCodeOption.SPE,
            StatusReasonCodeOption.TEMPPER,
            StatusReasonCodeOption.UNK
    );

    private static final List<StatusReasonCodeOption> TERMINATED_REASON_CODE_OPTIONS = List.of(
            StatusReasonCodeOption.AU,
            StatusReasonCodeOption.DEC,
            StatusReasonCodeOption.ERSRES,
            StatusReasonCodeOption.HON,
            StatusReasonCodeOption.LTP,
            StatusReasonCodeOption.LAP,
            StatusReasonCodeOption.MEDSTUD,
            StatusReasonCodeOption.NONPRAC,
            StatusReasonCodeOption.NR,
            StatusReasonCodeOption.ORG,
            StatusReasonCodeOption.OOP,
            StatusReasonCodeOption.RESDISC,
            StatusReasonCodeOption.RET,
            StatusReasonCodeOption.TI,
            StatusReasonCodeOption.TSF,
            StatusReasonCodeOption.UNK
    );

    /** Inactive / Nullified / Unknown Status Reason Code Options */
    private static final List<StatusReasonCodeOption> NULL_REASON_CODE_OPTIONS = List.of(
            StatusReasonCodeOption.MEDSTUD,
            StatusReasonCodeOption.ORG,
            StatusReasonCodeOption.OOP,
            StatusReasonCodeOption.UNK
    );

    private static final List<StatusReasonCodeOption> SUSPENDED_REASON_CODE_OPTIONS = List.of(
            StatusReasonCodeOption.AU,
            StatusReasonCodeOption.HON,
            StatusReasonCodeOption.LTP,
            StatusReasonCodeOption.LAP,
            StatusReasonCodeOption.MEDSTUD,
            StatusReasonCodeOption.MIS,
            StatusReasonCodeOption.NONPAY,
            StatusReasonCodeOption.NONPRAC,
            StatusReasonCodeOption.NR,
            StatusReasonCodeOption.ORG,
            StatusReasonCodeOption.OOP,
            StatusReasonCodeOption.RESDISC,
            StatusReasonCodeOption.RET,
            StatusReasonCodeOption.SUS,
            StatusReasonCodeOption.TI,
            StatusReasonCodeOption.UNK,
            StatusReasonCodeOption.VW
    );

    private static final List<StatusReasonCodeOption> PENDING_REASON_CODE_OPTIONS = List.of(
            StatusReasonCodeOption.INNONPRAC,
            StatusReasonCodeOption.MEDSTUD,
            StatusReasonCodeOption.NONPRAC,
            StatusReasonCodeOption.ORG,
            StatusReasonCodeOption.OOP,
            StatusReasonCodeOption.UNK
    );

    public static final Map<StatusCodeOption, List<StatusReasonCodeOption>> STATUS_REASON_CODE_OPTIONS_MAP = Map.of(
            StatusCodeOption.ACTIVE, ACTIVE_REASON_CODE_OPTIONS,
            StatusCodeOption.CANCELLED, CANCELLED_REASON_CODE_OPTIONS,
            StatusCodeOption.TERMINATED, TERMINATED_REASON_CODE_OPTIONS,
            StatusCodeOption.NULLIFIED, NULL_REASON_CODE_OPTIONS,
            StatusCodeOption.UNKNOWN, NULL_REASON_CODE_OPTIONS,
            StatusCodeOption.INACTIVE, NULL_REASON_CODE_OPTIONS,
            StatusCodeOption.SUSPENDED, SUSPENDED_REASON_CODE_OPTIONS,
            StatusCodeOption.PENDING, PENDING_REASON_CODE_OPTIONS
    );
}
