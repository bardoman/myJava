package com.ibm.sdwb.build390.configuration;

import com.ibm.sdwb.build390.LibraryError;

public interface ConfigurationAccess {

	public String getProjectConfigurationSetting(String section, String keyword)throws LibraryError;

	public java.util.Map getAllConfigurationSettings() throws LibraryError;
}
