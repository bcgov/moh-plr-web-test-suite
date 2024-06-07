package ca.bc.gov.health.qa.autotest.plr.web.pages.plr;

import java.util.Locale;

/**
 * TODO (AZ) - doc
 */
public enum ViewMode
{
    /**
     * Current View
     */
    CURRENT("Current"),

    /**
     * History View
     */
    HISTORY("History"),

    /**
     * Audit View
     */
    AUDIT("Audit");

    private final String itemText_;

    private ViewMode(String itemText)
    {
        itemText_ = itemText;
    }

    /**
     * TODO (AZ) - doc
     *
     * @return ???
     */
    public String getItemText()
    {
        return itemText_;
    }

    /**
     * TODO (AZ) - doc
     *
     * @param text
     *        ???
     *
     * @return ???
     *
     * @throws IllegalArgumentException
     *         ???
     */
    public static ViewMode parse(String text)
    {
        String value = text.toUpperCase(Locale.ROOT);
        int index = value.lastIndexOf(" VIEW");
        if (index > 0)
        {
            value = value.substring(0, index);
        }
        return ViewMode.valueOf(value);
    }
}
