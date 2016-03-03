package org.ovirt.engine.core.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class NetworkInSyncWithVdsNetworkInterfaceTest {

    private static final int DEFAULT_MTU_VALUE = 1500;
    private static final int VALUE_DENOTING_THAT_MTU_SHOULD_BE_SET_TO_DEFAULT_VALUE = 0;
    private static final Ipv4BootProtocol
            BOOT_PROTOCOL = Ipv4BootProtocol.forValue(RandomUtils.instance().nextInt(Ipv4BootProtocol.values().length));
    private static final String ADDRESS = "ADDRESS";
    private static final String NETMASK = "NETMASK";
    private static final String GATEWAY = "GATEWAY";
    private VdsNetworkInterface iface;
    private Network network;
    private HostNetworkQos ifaceQos;
    private HostNetworkQos networkQos;
    @Mock
    private IpConfiguration mockedIpConfiguration;
    @Mock
    private IPv4Address mockedIPv4Address;

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.DefaultMTU, 1500));

    @Before
    public void setUp() throws Exception {
        ifaceQos = new HostNetworkQos();
        networkQos = new HostNetworkQos();
        iface = new VdsNetworkInterface();
        //needed because network is vm network by default
        iface.setBridged(true);
        network = new Network();

        iface.setQos(ifaceQos);
    }

    @Test
    public void testIsNetworkInSyncWhenMtuDifferent() throws Exception {
        iface.setMtu(1);
        network.setMtu(2);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenMtuSameViaDefault() throws Exception {
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();

        iface.setMtu(DEFAULT_MTU_VALUE);
        network.setMtu(VALUE_DENOTING_THAT_MTU_SHOULD_BE_SET_TO_DEFAULT_VALUE);

        assertThat(testedInstanceWithSameNonQosValues.isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenVlanIdDifferent() throws Exception {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(2);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenBridgedFlagDifferent() throws Exception {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(1);

        iface.setBridged(true);
        network.setVmNetwork(false);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosEqual() throws Exception {
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosIsNull() throws Exception {
        iface.setQos(null);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNull() throws Exception {
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBothQosIsNull() throws Exception {
        iface.setQos(null);
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIfaceQosIsNullIfaceQosOverridden() throws Exception {
        iface.setQos(null);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenNetworkQosIsNullIfaceQosOverridden() throws Exception {
        networkQos = null;
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageLinkShareDifferent() throws Exception {
        ifaceQos.setOutAverageLinkshare(1);
        networkQos.setOutAverageLinkshare(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageUpperLimitDifferent() throws Exception {
        ifaceQos.setOutAverageUpperlimit(1);
        networkQos.setOutAverageUpperlimit(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenAverageRealTimeDifferent() throws Exception {
        ifaceQos.setOutAverageRealtime(1);
        networkQos.setOutAverageRealtime(2);
        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
    }

    public NetworkInSyncWithVdsNetworkInterface createTestedInstanceWithSameNonQosValues() {
        iface.setMtu(1);
        network.setMtu(1);

        iface.setVlanId(1);
        network.setVlanId(1);

        iface.setBridged(true);
        network.setVmNetwork(true);
        return createTestedInstance();
    }

    public NetworkInSyncWithVdsNetworkInterface createTestedInstance() {
        return new NetworkInSyncWithVdsNetworkInterface(iface, network, networkQos, mockedIpConfiguration);
    }

    @Test
    public void testReportConfigurationsOnHost() throws Exception {
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        ifaceQos.setOutAverageLinkshare(1);
        ifaceQos.setOutAverageUpperlimit(1);
        ifaceQos.setOutAverageRealtime(1);

        ReportedConfigurations reportedConfigurations = testedInstanceWithSameNonQosValues.reportConfigurationsOnHost();


        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        List<ReportedConfiguration> expectedReportedConfigurations = Arrays.asList(
                        new ReportedConfiguration(ReportedConfigurationType.MTU,
                                Integer.toString(iface.getMtu()),
                                Integer.toString(network.getMtu()),
                                true),
                        new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                                Boolean.toString(iface.isBridged()),
                                Boolean.toString(network.isVmNetwork()),
                                true),
                        new ReportedConfiguration(ReportedConfigurationType.VLAN,
                                Integer.toString(iface.getVlanId()),
                                Integer.toString(network.getVlanId()),
                                true),

                        new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE,
                                ifaceQos.getOutAverageLinkshare().toString(),
                                null,
                                false),
                        new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT,
                                ifaceQos.getOutAverageUpperlimit().toString(),
                                null,
                                false),
                        new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME,
                                ifaceQos.getOutAverageRealtime().toString(),
                                null,
                                false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(6));
    }
    @Test
    public void testReportConfigurationsOnHostWhenIfaceQosIsNull() throws Exception {
        ifaceQos = null;
        iface.setQos(null);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        networkQos.setOutAverageLinkshare(1);
        networkQos.setOutAverageUpperlimit(1);
        networkQos.setOutAverageRealtime(1);

        ReportedConfigurations reportedConfigurations = testedInstanceWithSameNonQosValues.reportConfigurationsOnHost();

        assertThat(createTestedInstanceWithSameNonQosValues().isNetworkInSync(), is(false));
        assertThat(reportedConfigurations.isNetworkInSync(), is(false));
        List<ReportedConfiguration> reportedConfigurationList = reportedConfigurations.getReportedConfigurationList();

        List<ReportedConfiguration> expectedReportedConfigurations = Arrays.asList(
            new ReportedConfiguration(ReportedConfigurationType.MTU,
                Integer.toString(iface.getMtu()),
                Integer.toString(network.getMtu()),
                true),
            new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                Boolean.toString(iface.isBridged()),
                Boolean.toString(network.isVmNetwork()),
                true),
            new ReportedConfiguration(ReportedConfigurationType.VLAN,
                Integer.toString(iface.getVlanId()),
                Integer.toString(network.getVlanId()),
                true),
            new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE,
                null,
                networkQos.getOutAverageLinkshare().toString(), false),
            new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME,
                null,
                networkQos.getOutAverageRealtime().toString(), false),
            new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT,
                null,
                networkQos.getOutAverageUpperlimit().toString(), false)
        );

        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testIsNetworkInSyncWhenIpConfigurationIsNull() throws Exception {
        mockedIpConfiguration = null;
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenIpConfigurationIsEmpty() throws Exception {
        when(mockedIpConfiguration.hasIpv4PrimaryAddressSet()).thenReturn(false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolEqual() throws Exception {
        initIpConfigurationBootProtocol(true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolDifferent() throws Exception {
        initIpConfigurationBootProtocol(false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    private void initIpConfigurationBootProtocol(boolean sameBootProtocol) {
        initIpConfiguration();
        when(mockedIPv4Address.getBootProtocol()).thenReturn(BOOT_PROTOCOL);
        Ipv4BootProtocol ifaceBootProtocol =
                sameBootProtocol ? BOOT_PROTOCOL : Ipv4BootProtocol.forValue((BOOT_PROTOCOL.getValue() + 1)
                        % Ipv4BootProtocol.values().length);
        iface.setIpv4BootProtocol(ifaceBootProtocol);
    }

    private void initIpConfiguration() {
        when(mockedIpConfiguration.hasIpv4PrimaryAddressSet()).thenReturn(true);
        when(mockedIpConfiguration.getIpv4PrimaryAddress()).thenReturn(mockedIPv4Address);
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolAddressEqual() throws Exception {
        initIpConfigurationBootProtocolAddress(BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolAddressDifferent() throws Exception {
        initIpConfigurationBootProtocolAddress(BOOT_PROTOCOL, false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolNetmaskEqual() throws Exception {
        initIpConfigurationBootProtocolNetmask(BOOT_PROTOCOL, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenStaticBootProtocolNetmaskDifferent() throws Exception {
        initIpConfigurationBootProtocolNetmask(BOOT_PROTOCOL, false);
        iface.setIpv4BootProtocol(Ipv4BootProtocol.forValue(
                (BOOT_PROTOCOL.getValue() + 1) % Ipv4BootProtocol.values().length));
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolNotStaticAddressDifferent() throws Exception {
        initIpConfigurationBootProtocolAddress(Ipv4BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenBootProtocolNotStaticNetmaskDifferent() throws Exception {
        initIpConfigurationBootProtocolNetmask(Ipv4BootProtocol.NONE, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenGatewayEqual(){
        initIpConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, true);
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenGatewayBothBlank() {
        List<String> blankValues = Arrays.asList(null, "");
        initIpConfigurationStaticBootProtocol(Ipv4BootProtocol.STATIC_IP);
        int blankIndex = RandomUtils.instance().nextInt(2);
        when(mockedIPv4Address.getGateway()).thenReturn(blankValues.get(blankIndex));
        iface.setIpv4Gateway(blankValues.get(blankIndex ^ 1));
        assertThat(createTestedInstance().isNetworkInSync(), is(true));
    }

    @Test
    public void testIsNetworkInSyncWhenGatewayDifferent(){
        initIpConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, false);
        assertThat(createTestedInstance().isNetworkInSync(), is(false));
    }


    @Test
    public void testReportConfigurationsOnHostWhenBootProtocolNotStatic() {
        initIpConfigurationBootProtocolAddress(Ipv4BootProtocol.NONE, false);
        initIpConfigurationBootProtocolNetmask(Ipv4BootProtocol.NONE, false);
        initIpConfigurationBootProtocolGateway(Ipv4BootProtocol.NONE, false);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        List<ReportedConfiguration> expectedReportedConfigurations = createDefaultExpectedReportedConfigurations();
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.BOOT_PROTOCOL,
                iface.getIpv4BootProtocol().name(),
                mockedIpConfiguration.getIpv4PrimaryAddress().getBootProtocol().name(),
                true));
        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    @Test
    public void testReportConfigurationsOnHostWhenBootProtocolStatic() {
        boolean syncAddress = RandomUtils.instance().nextBoolean();
        boolean syncNetmask = RandomUtils.instance().nextBoolean();
        boolean syncGateway = RandomUtils.instance().nextBoolean();
        initIpConfigurationBootProtocolAddress(Ipv4BootProtocol.STATIC_IP, syncAddress);
        initIpConfigurationBootProtocolNetmask(Ipv4BootProtocol.STATIC_IP, syncNetmask);
        initIpConfigurationBootProtocolGateway(Ipv4BootProtocol.STATIC_IP, syncGateway);
        NetworkInSyncWithVdsNetworkInterface testedInstanceWithSameNonQosValues =
                createTestedInstanceWithSameNonQosValues();
        List<ReportedConfiguration> reportedConfigurationList =
                testedInstanceWithSameNonQosValues.reportConfigurationsOnHost().getReportedConfigurationList();
        List<ReportedConfiguration> expectedReportedConfigurations = createDefaultExpectedReportedConfigurations();
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.BOOT_PROTOCOL,
                iface.getIpv4BootProtocol().name(),
                mockedIpConfiguration.getIpv4PrimaryAddress().getBootProtocol().name(),
                true));
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.NETMASK,
                iface.getIpv4Subnet(),
                mockedIpConfiguration.getIpv4PrimaryAddress().getNetmask(),
                syncNetmask));
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.IP_ADDRESS,
                iface.getIpv4Address(),
                mockedIpConfiguration.getIpv4PrimaryAddress().getAddress(),
                syncAddress));
        expectedReportedConfigurations.add(new ReportedConfiguration(ReportedConfigurationType.GATEWAY,
                iface.getIpv4Gateway(),
                mockedIpConfiguration.getIpv4PrimaryAddress().getGateway(),
                syncGateway));
        assertThat(reportedConfigurationList.containsAll(expectedReportedConfigurations), is(true));
        assertThat(reportedConfigurationList.size(), is(expectedReportedConfigurations.size()));
    }

    private void initIpConfigurationBootProtocolAddress(Ipv4BootProtocol ipv4BootProtocol, boolean syncAddress) {
        initIpConfigurationStaticBootProtocol(ipv4BootProtocol);
        when(mockedIPv4Address.getAddress()).thenReturn(ADDRESS);
        iface.setIpv4Address(syncAddress ? ADDRESS : null);
    }

    private void initIpConfigurationBootProtocolNetmask(Ipv4BootProtocol ipv4BootProtocol, boolean syncNetmask) {
        initIpConfigurationStaticBootProtocol(ipv4BootProtocol);
        when(mockedIPv4Address.getNetmask()).thenReturn(NETMASK);
        iface.setIpv4Subnet(syncNetmask ? NETMASK : null);
    }

    private void initIpConfigurationBootProtocolGateway(Ipv4BootProtocol ipv4BootProtocol, boolean syncGateway) {
        initIpConfigurationStaticBootProtocol(ipv4BootProtocol);
        when(mockedIPv4Address.getGateway()).thenReturn(GATEWAY);
        iface.setIpv4Gateway(syncGateway ? GATEWAY : null);
    }

    private void initIpConfigurationStaticBootProtocol(Ipv4BootProtocol ipv4BootProtocol) {
        initIpConfiguration();
        when(mockedIPv4Address.getBootProtocol()).thenReturn(ipv4BootProtocol);
        iface.setIpv4BootProtocol(ipv4BootProtocol);

    }

    private List<ReportedConfiguration> createDefaultExpectedReportedConfigurations() {
        List<ReportedConfiguration> defaultExpectedReportedConfigurations = Arrays.asList(new ReportedConfiguration(
                ReportedConfigurationType.MTU,
                Integer.toString(iface.getMtu()),
                Integer.toString(network.getMtu()),
                true),
                new ReportedConfiguration(ReportedConfigurationType.BRIDGED,
                        Boolean.toString(iface.isBridged()),
                        Boolean.toString(network.isVmNetwork()),
                        true),
                new ReportedConfiguration(ReportedConfigurationType.VLAN,
                        Integer.toString(iface.getVlanId()),
                        Integer.toString(network.getVlanId()),
                        true),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE, null, null, true),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME, null, null, true),
                new ReportedConfiguration(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT, null, null, true));
        return new LinkedList<>(defaultExpectedReportedConfigurations);
    }
}
