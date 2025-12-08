package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ca.bc.gov.health.qa.autotest.plr.web.pages.plr.provider.ProviderSection;

public class FacilityDataFields {
	private static final Map<FacilitySection, List<String>> SORT_KEY_MAP;
	static {
		Map<FacilitySection, List<String>> map = new EnumMap<>(FacilitySection.class);

		map.put(FacilitySection.IDENTIFIERS, List.of("Identifier"));
		map.put(FacilitySection.TELECOMMUNICATIONS, List.of("Type"));
		map.put(FacilitySection.ELECTRONIC_ADDRESSES, List.of("Type"));
		map.put(FacilitySection.NOTES, List.of("Note Identifier"));
		map.put(FacilitySection.ORGANIZATION_RELATIONSHIPS, List.of("Related Organization Identifier"));
		SORT_KEY_MAP = Collections.unmodifiableMap(map);
	}
	
	
	public static List<String> getSortKey(FacilitySection section)
    {
        return SORT_KEY_MAP.get(section);
    }
}
