package com.winterwell.datalog;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.winterwell.depot.IInit;
import com.winterwell.utils.containers.Containers;
import com.winterwell.utils.io.DBOptions;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.io.Option;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.time.Dt;
import com.winterwell.utils.time.TUnit;

/**
 * Why is this in utils? Because the interface references it.
 * 
 * This might include DB connection options -- but it does not have to, provided those are set
	 * elsewhere.
	 * 
	 * 
	 * 
We can send different datalog namespaces to different ES.

E.g. we might send tracking (which is in the default namespace, using ES index datalog.default) to one ES, 
whilst the lower volume advert watching (namespace=goodloop, ES index=datalog.goodloop -- spot the pattern) goes to a different one.

This is setup via the file `config/datalog.{namespace}.properties`, changing the defaults:
port=9200
server=localhost

	 * 
 * @author daniel
 *         <p>
 *         <b>Copyright & license</b>: (c) Winterwell Associates Ltd, all rights
 *         reserved. This class is NOT formally a part of the com.winterwell.utils
 *         library. In particular, licenses for the com.winterwell.utils library do
 *         not apply to this file.
 */
public class DataLogConfig extends DBOptions implements IInit { 

	public DataLogConfig() {
		Log.d("DataLogConfig");
	}
	
	public int maxDataPoints = 10000;

	/**
	 * Bucket size. Also the gap between saves.
	 */
	@Option(description = "Bucket size. Also the gap between saves.")
	public Dt interval = 
//			new Dt(0.1, TUnit.MINUTE); // for testing
			new Dt(15, TUnit.MINUTE);

	/**
	 * Normally "default", This sets {@link DataLog#DEFAULT_DATASPACE}
	 */
	@Option(description = "namespace: if set, use a separate namespace (to avoid race-condition overwriting of stats with another JVM).")
	public String namespace = "default";
	
	/**
	 * These namespaces have their own config overrides.
	 */
	@Option
	public List<String> namespaceConfigs = Arrays.asList("default");

	public Dt filePeriod = TUnit.DAY.dt;
	
	@Option
	public Class storageClass;
	

	/**
	 * Jetty server port for incoming logging
	 */
	@Option
	public int port = 8585;
	
	
	@Option
	public String COOKIE_DOMAIN = ".good-loop.com";


	@Option
	public File logFile = new File(FileUtils.getWorkingDirectory(), "lg.txt"); 


	@Option(description="If true, Java will set CORS cross-domain access headers. Note that this can cause bugs if NGinx is also setting them.")
	public boolean CORS = true;

	@Option(description="Used for logging to a remote server (provided storageClass has been set to use one)")
	public String logEndpoint;

	public String getDataEndpoint;

	Map<String, Object> tagHandlers = new HashMap();

	@Option
	public boolean noCallbacks;
	
	public void setTagHandler(String tag, Supplier supplier) {
		tagHandlers.put(tag, supplier);
	}

	@Override
	public void init() {
		try {
			// convert settings into supplier functions
			// status: not yet used!
			for(String k : tagHandlers.keySet()) {
				Object v = tagHandlers.get(k);
				if (v instanceof String) {
					v = Class.forName((String) v);
				}
				if (v instanceof Class) {
					v = ((Class) v).getConstructor();
					tagHandlers.put(k, v);
				}
			}
		} catch(Throwable ex) {
			// swallow and carry on
			Log.e("DataLogConfig", ex);
		}
	}

	public Supplier getTagHandler(String topTag) {
		Object th = tagHandlers.get(topTag);
		return (Supplier) th;
	}

}
