package lcmc.common.domain.util;

import static junitparams.JUnitParamsRunner.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import javax.inject.Provider;
import javax.swing.JPanel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lcmc.Exceptions;
import lcmc.HwEventBus;
import lcmc.cluster.service.ssh.Ssh;
import lcmc.cluster.service.storage.BlockDeviceService;
import lcmc.common.domain.Application;
import lcmc.common.ui.main.MainData;
import lcmc.common.ui.main.ProgressIndicator;
import lcmc.common.ui.utils.SwingUtils;
import lcmc.drbd.domain.DrbdXml;
import lcmc.drbd.ui.resource.GlobalInfo;
import lcmc.host.domain.Host;
import lcmc.host.domain.HostFactory;
import lcmc.host.domain.Hosts;
import lcmc.host.ui.HostBrowser;
import lcmc.host.ui.TerminalPanel;
import lcmc.robotest.RoboTest;
import lcmc.vm.domain.VmsXml;

@RunWith(JUnitParamsRunner.class)
public final class ToolsTest {
    @InjectMocks
    private HostFactory hostFactory;
    @Mock
    private HwEventBus hwEventBus;
    @Mock
    private SwingUtils swingUtils;
    @Mock
    private Application application;
    @Mock
    private MainData mainData;
    @Mock
    private ProgressIndicator progressIndicator;
    @Mock
    private Hosts allHosts;
    @Mock
    private RoboTest roboTest;
    @Mock
    private BlockDeviceService blockDeviceService;

    @Mock
    private Provider<VmsXml> vmsXmlProvider;
    @Mock
    private VmsXml vmsXml;
    @Mock
    private Provider<DrbdXml> drbdXmlProvider;
    @Mock
    private DrbdXml drbdXml;
    @Mock
    private Provider<TerminalPanel> terminalPanelProvider;
    @Mock
    private TerminalPanel terminalPanel;
    @Mock
    private Provider<Ssh> sshProvider;
    @Mock
    private Ssh ssh;
    @Mock
    private Provider<HostBrowser> hostBrowserProvider;
    @Mock
    private HostBrowser hostBrowser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(vmsXmlProvider.get()).thenReturn(vmsXml);
        when(drbdXmlProvider.get()).thenReturn(drbdXml);
        when(terminalPanelProvider.get()).thenReturn(terminalPanel);
        when(sshProvider.get()).thenReturn(ssh);
        when(hostBrowserProvider.get()).thenReturn(hostBrowser);
        Tools.init();
    }

    @Test
    public void testCreateImageIcon() {
        assertNull("not existing", Tools.createImageIcon("notexisting"));
        assertNotNull("existing", Tools.createImageIcon("startpage_head.jpg"));
    }

    @Test
    public void testSetDefaults() {
        Tools.setDefaults();
    }

    @Test
    @Parameters({"127.0.0.1",
                 "0.0.0.0",
                 "0.0.0.1",
                 "255.255.255.255",
                 "254.255.255.255"})
    public void testIsIp(final String ip) {
        assertThat(Tools.isIp(ip)).isTrue();
    }

    @Test
    @Parameters({"localhost",
                 "127-0-0-1",
                 "256.255.255.255",
                 "255.256.255.255",
                 "255.255.256.255",
                 "255.255.255.256",
                 "255.255.255.1000",
                 "255.255.255.-1",
                 "255.255.255",
                 "",
                 "255.255.255.255.255",
                 "127.0.0.false",
                 "127.0.false.1",
                 "127.false.0.1",
                 "false.0.0.1"})
    public void testIsNotIp(final String ip) {
        assertThat(Tools.isIp(ip)).isFalse();
    }

    @Test
    public void testPrintStackTrace() {
        assertThat("".equals(Tools.getStackTrace())).isFalse();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForDefaultShouldBeReturned() {
        return $(
            $("", "SSH.PublicKey"),
            $("", "SSH.PublicKey"),
            $("22", "SSH.Port")
        );
    }

    @Test
    @Parameters(method="parametersForDefaultShouldBeReturned")
    public void defaultShouldBeReturned(final String default0, final String key) {
        assertThat(Tools.getDefault(key)).isEqualTo(default0);
    }

    @Test
    public void testGetDefaultColor() {
        assertThat(Tools.getDefaultColor("TerminalPanel.Background")).isEqualTo(java.awt.Color.BLACK);
    }

    @Test
    public void testGetDefaultInt() {
        assertThat(Tools.getDefaultInt("Score.Infinity")).isEqualTo(100000);
    }

    @Test
    public void testGetString() {
        assertThat(Tools.getString("DrbdMC.Title")).isEqualTo("Linux Cluster Management Console");
    }

    @Test
    public void testGetErrorString() {
        final String errorString = "the same string";
        assertThat(errorString).isEqualTo(errorString);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForTestJoin() {
        return $(
            $("a,b",   ",",  new String[]{"a", "b"}),
            $("a",     ",",  new String[]{"a"}),
            $("",      ",",  new String[]{}),
            $("",      ",", null),
            $("ab",    null, new String[]{"a", "b"}),
            $("a,b,c", ",",  new String[]{"a", "b" , "c"}),
            $("a",     ",",  new String[]{"a", null}),
            $("",      ",",  new String[]{null, null}),
            $("",      ",",  new String[]{null, null}),
            $("a",     ",",  new String[]{"a", null, null}),
            $("a",     ",",  new String[]{null, "a", null}),
            $("a",     ",",  new String[]{null, null, "a"})
        );
    }

    @Test
    @Parameters(method="parametersForTestJoin")
    public void testJoin(final String expected, final String delim, final String[] values) {
        assertThat(Tools.join(delim, values)).isEqualTo(expected);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForTestJoinWithLength() {
        return $(
            $("a,b",   ",", new String[]{"a", "b"},      2),
            $("a,b",   ",", new String[]{"a", "b"},      3),
            $("a",     ",", new String[]{"a", "b"},      1),
            $("",      ",", new String[]{"a", "b"},      0),
            $("",      ",", new String[]{"a", "b"},      -1),
            $("",      ",", null,                        1),
            $("a",     ",", new String[]{"a"},           1),
            $("",      ",", new String[]{},              2),
            $("",      ",", null,                        1),
            $("a,b,c", ",", new String[]{"a", "b", "c"}, 3)
        );
    }

    @Test
    @Parameters(method="parametersForTestJoinWithLength")
    public void joinWithLengthShouldWork(final String expected,
                                         final String delim,
                                         final String[] values,
                                         final int length) {
        assertThat(Tools.join(delim, values, length)).isEqualTo(expected);
    }


    @Test
    public void joinCollectionShouldWork() {
        assertThat(Tools.join(null, Arrays.asList("a", "b"))).isEqualTo("ab");
    }

    @Test
    public void joinBigArrayShouldWork() {
        final List<String> bigArray = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            bigArray.add("x");
        }
        assertThat(Tools.join(",", bigArray)).hasSize(2000 - 1);
        assertThat(Tools.join(",", bigArray.toArray(new String[bigArray.size()]), 500)).hasSize(1000 - 1);
    }

    @Test
    @Parameters({"Rasto, rasto",
                 "Rasto, Rasto",
                 "RASTO, RASTO"})
    public void testUCFirst(final String expected, final String anyString) {
        assertThat(Tools.ucfirst(anyString)).isEqualTo(expected);
    }

    @Test
    public void ucFirstNullShouldBeNull() {
        assertNull(Tools.ucfirst(null));
    }

    @Test
    public void ucFirstEmptyStringShouldBeEmptyString() {
        assertThat(Tools.ucfirst("")).isEqualTo("");
    }


    @SuppressWarnings("unused")
    private Object[] parametersForHtmlShouldBeCreated() {
        return $( 
            $("<html><p>test\n</html>", "test"),
            $("<html><p>test<br>line2\n</html>", "test\nline2"),
            $("<html>\n</html>", null)
        );
    }

    @Test
    @Parameters(method="parametersForHtmlShouldBeCreated")
    public void htmlShouldBeCreated(final String html, final String text) {
        assertThat(Tools.html(text)).isEqualTo(html);
    }


    @SuppressWarnings("unused")
    private Object[] parametersForShouldBeStringClass() {
        return $( 
            $("string"),
            $((String) null),
            $((Object) null)
        );
    }

    @Test
    @Parameters(method="parametersForShouldBeStringClass")
    public void shouldBeStringClass(final Object object) {
        assertThat(Tools.isStringClass(object)).isTrue();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldNotBeStringClass() {
        return $( 
            $(new Object()),
            $(new StringBuilder())
        );
    }

    @Test
    @Parameters(method="parametersForShouldNotBeStringClass")
    public void shouldNotBeStringClass(final Object object) {
        assertThat(Tools.isStringClass(object)).isFalse();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForConfigShouldBeEscaped() {
        return $( 
            $(null,                  null), 
            $("",                    ""), 
            $("\"\\\"\"",            "\""), 
            $("text",                "text"), 
            $("\"text with space\"", "text with space"), 
            $("\"text with \\\"\"",  "text with \""), 
            $("\"just\\\"\"",        "just\"")
        );
    }

    @Test
    @Parameters(method="parametersForConfigShouldBeEscaped")
    public void configShouldBeEscaped(final String escaped, final String config) {
        assertThat(Tools.escapeConfig(config)).isEqualTo(escaped);
    }

    @Test
    public void testSetSize() {
        final JPanel p = new JPanel();
        Tools.setSize(p, 20, 10);
        assertThat(p.getMaximumSize()).isEqualTo(new Dimension(Short.MAX_VALUE, 10));
        assertThat(p.getMinimumSize()).isEqualTo(new Dimension(20, 10));
        assertThat(p.getPreferredSize()).isEqualTo(new Dimension(20, 10));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForFirstVersionShouldBeSmaller() {
        return $( 
            $("2.1.3", "2.1.4"),
            $("2.1.3", "3.1.2"),
            $("2.1.3", "2.2.2"),
            $("2.1.3.1", "2.1.4"),

            $("8.3.9", "8.3.10rc1"),
            $("8.3.10rc1", "8.3.10rc2"),
            $("8.3.10rc2", "8.3.10"),
            $("8.3", "8.4"),
            $("8.3", "8.4.5"),
            $("8.3.5", "8.4"),
            $("8.3", "8.4rc3"),
            $("1.1.7-2.fc16", "1.1.8"),
            $("1.6.0_26", "1.7")
        );
    }

    @Test
    @Parameters(method="parametersForFirstVersionShouldBeSmaller")
    public void firstVersionShouldBeSmaller(final String versionOne, final String versionTwo)
    throws Exceptions.IllegalVersionException {
        assertThat(Tools.compareVersions(versionOne, versionTwo)).isEqualTo(-1);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForFirstVersionShouldBeGreater() {
        return $( 
            $("2.1.4", "2.1.3"),
            $("3.1.2", "2.1.3"),
            $("2.2.2", "2.1.3"),
            $("2.1.4", "2.1.3.1"),
            $("8.3.10rc1", "8.3.9"),
            $("8.3.10rc2", "8.3.10rc1"),
            $("8.3.10", "8.3.10rc2"),
            $("8.3.10", "8.3.10rc99999999"),
            $("8.4", "8.3"),
            $("8.4rc3", "8.3"),
            $("1.1.7-2.fc16", "1.1.6"),
            $("1.7", "1.6.0_26")
        );
    }

    @Test
    @Parameters(method="parametersForFirstVersionShouldBeGreater")
    public void firstVersionShouldBeGreater(final String versionOne, final String versionTwo)
    throws Exceptions.IllegalVersionException {
        assertThat(Tools.compareVersions(versionOne, versionTwo)).isEqualTo(1);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForVersionsShouldBeEqual() {
        return $( 
            $("2.1.3", "2.1.3.1"),
            $("2.1", "2.1.3"),
            $("2", "2.1.3"),
            $("2", "2.1"),

            $("2.1.3", "2.1.3"),
            $("2.1", "2.1"),
            $("2", "2"),
            $("2.1.3.1", "2.1.3"),
            $("2.1.3", "2.1"),
            $("2.1.3", "2"),
            $("2.1", "2"),
            $("8.3", "8.3.0"),

            $("8.3.10rc1", "8.3.10rc1"),
            $("8.3rc1", "8.3rc1"),
            $("8rc1", "8rc1"),
            $("8.3rc2", "8.3.0"),
            $("8.3", "8.3.2"),
            $("8.3.2", "8.3"),
            $("8.4", "8.4"),
            $("8.4", "8.4.0rc3"),
            $("8.4.0rc3", "8.4"),
            $("1.1.7-2.fc16", "1.1.7"),
            $("1.7.0_03", "1.7"),
            $("1.6.0_26", "1.6.0")
        );
    }

    @Test
    @Parameters(method="parametersForVersionsShouldBeEqual")
    public void versionsShouldBeEqual(final String versionOne, final String versionTwo)
    throws Exceptions.IllegalVersionException {
        assertThat(Tools.compareVersions(versionOne, versionTwo)).isEqualTo(0);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForCompareVersionsShouldThrowException() {
        return $( 
            $("", ""),
            $(null, null),
            $("", "2.1.3"),
            $("2.1.3", ""),
            $(null, "2.1.3"),
            $("2.1.3", null),
            $("2.1.3", "2.1.a"),
            $("a.1.3", "2.1.3"),
            $("rc1", "8rc1"),
            $("8rc1", "8rc"),
            $("8rc1", "8rc"),
            $("8rc", "8rc1"),
            $("8rc1", "rc"),
            $("rc", "8rc1"),
            $("8r1", "8.3.1rc1"),
            $("8.3.1", "8.3rc1.1"),
            $("8.3rc1.1", "8.3.1")
        );
    }

    @Test(expected=Exceptions.IllegalVersionException.class)
    @Parameters(method="parametersForCompareVersionsShouldThrowException")
    public void compareVersionsShouldThrowException(final String versionOne, final String versionTwo)
    throws Exceptions.IllegalVersionException {
        Tools.compareVersions(versionOne, versionTwo);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForCharCountShouldBeReturned() {
        return $( 
            $(1, "abcd", 'b'),
            $(0, "abcd", 'e'),
            $(1, "abcd", 'd'),
            $(1, "abcd", 'a'),
            $(2, "abcdb", 'b'),
            $(5, "ccccc", 'c'),
            $(1, "a", 'a'),
            $(0, "a", 'b'),
            $(0, "", 'b')
        );
    }

    @Test
    @Parameters(method="parametersForCharCountShouldBeReturned")
    public void charCountShouldBeReturned(final int count, final String string, final char character) {
        assertThat(Tools.charCount(string, character)).isEqualTo(count);
    }

    @Test
    public void charCountInNullShouldReturnZero() {
        assertThat(Tools.charCount(null, 'b')).isEqualTo(0);
    }

    @Test
    @Parameters({"1", "-1", "0", "-0", "1235", "100000000000000000", "-100000000000000000"})
    public void shouldBeNumber(final String number) {
        assertThat(Tools.isNumber(number)).isTrue();
    }

    @Test
    @Parameters({"0.1", "1 1", "-", "", "a", ".5", "a1344", "1344a", "13x44"})
    public void shouldNotBeNumber(final String number) {
        assertThat(Tools.isNumber(number)).isFalse();
    }

    @Test
    public void nullShouldNotBeNumber() {
        assertThat(Tools.isNumber(null)).isFalse();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShellListShouldBeCreated() {
        return $( 
            $("{'a','b'}", new String[]{"a", "b"}),
            $("{'a','b','b'}", new String[]{"a", "b", "b"}),
            $("a", new String[]{"a"}),
            $(null, new String[]{}),
            $(null, null)
        );
    }

    @Test
    @Parameters(method="parametersForShellListShouldBeCreated")
    public void shellListShouldBeCreated(final String shellList, final String[] list) {
        assertThat(Tools.shellList(list)).isEqualTo(shellList);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForStringsShouldBeEqual() {
        return $( 
            $(null, null),
            $("", ""),
            $("x", "x")
        );
    }

    @Test
    @Parameters(method="parametersForStringsShouldBeEqual")
    public void stringsShouldBeEqual(final String stringOne, final String stringTwo) {
        assertThat(stringTwo).isEqualTo(stringOne);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForStringsShouldNotBeEqual() {
        return $(
            $("x", "a"),
            $("x", ""),
            $("", "x"),
            $(null, "x"),
            $("x", null)
        );
    }

    @Test
    @Parameters(method="parametersForStringsShouldNotBeEqual")
    public void stringsShouldNotBeEqual(final String stringOne, final String stringTwo) {
        assertNotEquals(stringOne, stringTwo);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForUnitShouldBeExtracted() {
        return $(
            $("10", "min", "10min"),
            $("0",  "s",   "0s"),
            $("0",  "",    "0"),
            $("5",  "",    "5"),
            $("",   "s",   "s"),
            $(null, null,  null)
        );
    }

    @Test
    public void testGetRandomSecret() {
        for (int i = 0; i < 100; i++) {
            final String s = Tools.getRandomSecret(2000);
            assertThat(s).hasSize(2000);
            final int count = Tools.charCount(s, 'a');
            assertThat(count > 2 && count < 500).isTrue();
        }
    }

    @Test
    @Parameters({"127.0.0.1", "127.0.1.1"})
    public void testIsLocalIp(final String ip) {
        assertThat(Tools.isLocalIp(ip)).isTrue();
    }

    @Test
    @Parameters({"127.0.0", "127.0.0.1.1", "127.0.0.a", "a", "a"})
    public void testIsNotLocalIp(final String ip) {
        assertThat(Tools.isLocalIp(ip)).isFalse();
    }

    @Test
    public void textShouldBeTrimmed() {
        assertNull(Tools.trimText(null));
        assertThat(Tools.trimText("x")).isEqualTo("x");
        final String x20 = " xxxxxxxxxxxxxxxxxxx";
        assertThat(Tools.trimText(x20 + x20 + x20 + x20)).isEqualTo(x20 + x20 + x20 + x20);
    }

    @Test
    public void textShouldNotBeTrimmed() {
        assertNull(Tools.trimText(null));
        assertThat(Tools.trimText("x")).isEqualTo("x");
        final String x20 = " xxxxxxxxxxxxxxxxxxx";
        assertThat(Tools.trimText(x20 + x20 + x20 + x20 + x20)).isEqualTo(x20 + x20 + x20 + x20 + "\n" + x20.trim());
    }

    @SuppressWarnings("unused")
    private Object[] parametersForDirectoryPartShouldBeExtracted() {
        return $(
            $("/usr/bin/", "/usr/bin/somefile"),
            $("/usr/bin/", "/usr/bin/"),
            $("somefile", "somefile"),
            $("", ""),
            $(null, null),
            $("/", "/")
        );
    }

    @Test
    @Parameters(method="parametersForDirectoryPartShouldBeExtracted")
    public void directoryPartShouldBeExtracted(final String extractedDir, final String file) {
        assertThat(Tools.getDirectoryPart(file)).isEqualTo(extractedDir);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForQuotesShouldBeEscaped() {
        return $(
            $("test", "test", 0),
            $("test", "test", -1),
            $(null, null, -1),
            $(null, null, 1),

            $("test", "test", 1),
            $("test", "test", 2),
            $("test", "test", 100),

            $("\\\"\\$\\`test\\\\", "\"$`test\\", 1),
            $("\\\\\\\"\\\\\\$\\\\\\`test\\\\\\\\", "\"$`test\\", 2)
        );
    }

    @Test
    @Parameters(method="parametersForQuotesShouldBeEscaped")
    public void quotesShouldBeEscaped(final String escaped, final String string, final int level) {
        assertThat(Tools.escapeQuotes(string, level)).isEqualTo(escaped);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForTestVersionBeforePacemaker() {
        return $(
            $(null, "2.1.4"),
            $(null, "2.1.3")
        );
    }

    @Test
    @Parameters(method="parametersForTestVersionBeforePacemaker")
    public void testVersionBeforePacemaker(final String pcmkVersion, final String hbVersion) {
        final GlobalInfo globalInfo = new GlobalInfo();
        final Host host = hostFactory.createInstance();

        host.getHostParser().setPacemakerVersion(pcmkVersion);
        host.getHostParser().setHeartbeatVersion(hbVersion);
        assertThat(Tools.versionBeforePacemaker(host)).isTrue();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForTestVersionAfterPacemaker() {
        return $(
            $("1.1.5", null),
            $(null, null),
            $("1.0.9", "3.0.2"),
            $("1.0.9", "2.99.0"),
            $("1.0.9", null)
        );
    }

    @Test
    @Parameters(method="parametersForTestVersionAfterPacemaker")
    public void testVersionAfterPacemaker(final String pcmkVersion, final String hbVersion) {
        final Host host = hostFactory.createInstance();

        host.getHostParser().setPacemakerVersion(pcmkVersion);
        host.getHostParser().setHeartbeatVersion(hbVersion);
        assertThat(Tools.versionBeforePacemaker(host)).isFalse();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForTwoNewLineShouldBeOne() {
        return $(
            $("",      ""),
            $("\n",    "\n\n\n"),
            $(" ",     " "),
            $("a",     "a"),
            $("a\nb",  "a\nb"),
            $(" a\n",  " a\n"),
            $(" a\n",  " a\n\n"),
            $(" a \n", " a \n")
        );
    }

    @Test
    @Parameters(method="parametersForTwoNewLineShouldBeOne")
    public void twoNewLineShouldBeOne(final String chomped, final String origString) {
        final StringBuffer sb = new StringBuffer(origString);
        Tools.chomp(sb);
        assertThat(sb.toString()).isEqualTo(chomped);
    }

    @Test
    public void testGenerateVMMacAddress() {
       final String mac = Tools.generateVMMacAddress();
       assertThat(17).isEqualTo(mac.length());
    }

    @SuppressWarnings("unused")
    private Object[] parametersForNamesShouldBeTheSame() {
        return $(
            $("a", "a"),
            $("2a", "2a"),
            $("1a2b3c4", "1a2b3c4"),
            $(null, null)
        );
    }

    @Test
    @Parameters(method="parametersForNamesShouldBeTheSame")
    public void namesShouldBeTheSame(final String nameOne, final String nameTwo) {
        assertThat(Tools.compareNames(nameOne, nameTwo) == 0).isTrue();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForNameOneShouldBeSmaller() {
        return $(
            $("a", "b"),
            $("1a", "2a"),
            $("2a", "2a1"),
            $("a2b", "a10b"),
            $("a2b3", "a10b"),
            $("a2b", "a10b3"),
            $("", "a"),
            $(null, "1"),
            $("1x", "Node001")
        );
    }

    @Test
    @Parameters(method="parametersForNameOneShouldBeSmaller")
    public void nameOneShouldBeSmaller(final String nameOne, final String nameTwo) {
        assertThat(Tools.compareNames(nameOne, nameTwo) < 0).isTrue();
    }

    @SuppressWarnings("unused")
    private Object[] parametersForNameOneShouldBeGreater() {
        return $(
            $("10a", "2a"),
            $("2a1", "2a"),
            $("a10", "a2"),
            $("a10b", "a2b"),
            $("a", ""),
            $("1", ""),
            $("1", null)
        );
    }

    @Test
    @Parameters(method="parametersForNameOneShouldBeGreater")
    public void nameOneShouldBeGreater(final String nameOne, final String nameTwo) {
        assertThat(Tools.compareNames(nameOne, nameTwo) > 0).isTrue();
    }

    private Object[] equalCollections() {
        return $(
                $(new ArrayList<String>(), new ArrayList<String>()),
                $(new ArrayList<String>(Arrays.asList("a", "b")), new ArrayList<String>(Arrays.asList("a", "b"))),
                $(new TreeSet<String>(), new TreeSet<String>()),
                $(new TreeSet<String>(Arrays.asList("a", "b")), new TreeSet<String>(Arrays.asList("a", "b"))),
                $(new TreeSet<String>(Arrays.asList("b", "a")), new TreeSet<String>(Arrays.asList("a", "b"))));
    }

    @Test
    @Parameters(method="equalCollections")
    public void collectionsShouldBeEqual(final Collection<String> collection1, Collection<String> collection2) {
        assertThat(Tools.equalCollections(collection1, collection2)).isTrue();
    }

    private Object[] unequalCollections() {
        return $(
                $(new ArrayList<String>(), new ArrayList<>(Arrays.asList("a"))),
                $(new ArrayList<>(Arrays.asList("a")), new ArrayList<String>()),
                $(new ArrayList<>(Arrays.asList("a")), new ArrayList<>(Arrays.asList("a", "b"))),
                $(new ArrayList<>(Arrays.asList("a", "b")), new ArrayList<>(Arrays.asList("b"))),
                $(new ArrayList<>(Arrays.asList("a", "a")), new ArrayList<>(Arrays.asList("a", "b"))),
                $(new ArrayList<>(Arrays.asList("b", "b")), new ArrayList<>(Arrays.asList("a", "b"))),
                $(new TreeSet<String>(), new TreeSet<>(Arrays.asList("a"))),
                $(new TreeSet<>(Arrays.asList("a")), new TreeSet<String>()),
                $(new TreeSet<>(Arrays.asList("a")), new TreeSet<>(Arrays.asList("a", "b"))),
                $(new TreeSet<>(Arrays.asList("a", "b")), new TreeSet<>(Arrays.asList("b"))),
                $(new TreeSet<>(Arrays.asList("a", "a")), new TreeSet<>(Arrays.asList("a", "b"))),
                $(new TreeSet<>(Arrays.asList("b", "b")), new TreeSet<>(Arrays.asList("a", "b"))));
    }

    @Test
    @Parameters(method="unequalCollections")
    public void collectionsShouldNotBeEqual(final Collection<String> collection1, Collection<String> collection2) {
        assertThat(Tools.equalCollections(collection1, collection2)).isFalse();
    }
}
