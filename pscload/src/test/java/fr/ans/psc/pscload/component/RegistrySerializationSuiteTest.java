package fr.ans.psc.pscload.component;


import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({RegistrySerializationTest.class, RegistryDeserializationTest.class})
public class RegistrySerializationSuiteTest {
}
