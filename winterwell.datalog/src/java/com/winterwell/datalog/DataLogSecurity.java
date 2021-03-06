/**
 * 
 */
package com.winterwell.datalog;

import java.util.List;

import com.winterwell.utils.Dep;
import com.winterwell.web.ajax.AjaxMsg;
import com.winterwell.web.app.WebRequest;
import com.winterwell.web.data.XId;
import com.winterwell.youagain.client.AuthToken;
import com.winterwell.youagain.client.YouAgainClient;

/**
 * @author daniel
 *
 */
public class DataLogSecurity {

	public static void check(WebRequest state, Dataspace dataspace, List<String> breakdown) {
		// TODO insist on login
//		if (true) return;
		XId user = state.getUserId();
		if (user==null) {
			YouAgainClient yac = Dep.get(YouAgainClient.class);
			List<AuthToken> u = yac.getAuthTokens(state);
			if (u==null) {
				state.addMessage(new AjaxMsg(new SecurityException("DataLogSecurity: not logged in")));
			}
		}
		
		// TODO check shares with YouAgain -- all the authd XIds		
		
		// assume all OK!
	}

}
