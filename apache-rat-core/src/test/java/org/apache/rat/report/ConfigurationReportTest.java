package org.apache.rat.report;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;

public class ConfigurationReportTest {

    
   @Test
   public void testAll() throws RatException, IOException {
       ReportConfiguration reportConfiguration = new ReportConfiguration(DefaultLog.INSTANCE);
       reportConfiguration.setFrom(Defaults.builder().build());
       reportConfiguration.listFamilies(LicenseFilter.all);
       reportConfiguration.listLicenses(LicenseFilter.all);
       StringWriter sw = new StringWriter();
       IXmlWriter writer = new XmlWriter(sw);
       
       ConfigurationReport report = new ConfigurationReport(writer, reportConfiguration);
       report.startReport();
       report.endReport();
       writer.closeDocument();
       System.out.println( sw );
       
   }
}
