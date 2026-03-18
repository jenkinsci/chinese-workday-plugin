package io.jenkins.plugins.chinese_workday.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ChineseWorkdayGlobalConfigurationUiTest {

    @Test
    void globalConfigurationPageLoads(JenkinsRule jenkins) throws Exception {
        HtmlPage page = jenkins.createWebClient().goTo("manage/configure");

        String text = page.asNormalizedText();
        assertTrue(text.contains("Chinese Workday"));
        assertTrue(text.contains("Add calendar"));
    }

    @Test
    void addCalendarButtonRevealsCalendarFields(JenkinsRule jenkins) throws Exception {
        HtmlPage page = jenkins.createWebClient().goTo("manage/configure");

        // Jenkins repeatable controls may render the add action as either a button or a link.
        HtmlElement addCalendarButton = page.getFirstByXPath("//button[contains(normalize-space(.), 'Add calendar')]");
        if (addCalendarButton == null) {
            addCalendarButton = page.getFirstByXPath("//a[contains(normalize-space(.), 'Add calendar')]");
        }

        // Clicking the repeatable control mutates the form in place, so assert on the updated page content.
        page = addCalendarButton.click();
        jenkins.waitUntilNoActivity();

        String text = page.asNormalizedText();
        assertTrue(text.contains("Year"));
        assertTrue(text.contains("Holidays"));
        assertTrue(text.contains("Make-up workdays"));
    }

    @Test
    void yearHelpPageIsAvailable(JenkinsRule jenkins) throws Exception {
        HtmlPage page = jenkins.createWebClient().goTo("plugin/chinese-workday/help/chinese-workday-global/year.html");

        assertTrue(page.asNormalizedText().contains("Enter the target calendar year"));
    }

    @Test
    void holidaysHelpPageIsAvailable(JenkinsRule jenkins) throws Exception {
        HtmlPage page =
                jenkins.createWebClient().goTo("plugin/chinese-workday/help/chinese-workday-global/holidays.html");

        assertTrue(page.asNormalizedText().contains("Enter Chinese statutory holidays"));
    }

    @Test
    void makeUpWorkdaysHelpPageIsAvailable(JenkinsRule jenkins) throws Exception {
        HtmlPage page = jenkins.createWebClient()
                .goTo("plugin/chinese-workday/help/chinese-workday-global/make-up-workdays.html");

        assertTrue(page.asNormalizedText().contains("treated as workdays"));
    }
}
