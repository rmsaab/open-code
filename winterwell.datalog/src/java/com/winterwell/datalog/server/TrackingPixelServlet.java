package com.winterwell.datalog.server;

import java.io.File;
import java.io.IOException;

import com.winterwell.datalog.DataLogConfig;
import com.winterwell.datalog.Dataspace;
import com.winterwell.utils.Dep;
import com.winterwell.utils.Utils;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.time.Dt;
import com.winterwell.utils.time.TUnit;
import com.winterwell.web.app.FileServlet;
import com.winterwell.web.app.IServlet;
import com.winterwell.web.app.WebRequest;
import com.winterwell.web.data.XId;
import com.winterwell.youagain.client.AuthToken;

/**
 * Call via <img src='https://lg.good-loop.com/pxl' style='position:absolute;bottom:0px;left:0px;width:1px;height:1px;' />
 * 
 * pxl event
 * Defaults to dataspace "trk" but allow override
 * 
 * Referer url: explicitly set via ref=, or the referring page from the http header
 * 
 * DoNotTrack is handled by {@link WebRequest#isDoNotTrack()}
 * 
 * @author daniel
 *
 */
public class TrackingPixelServlet implements IServlet {
	public static final String trkid = "trkid";
	private static final long serialVersionUID = 1L;
	static final String DATALOG_EVENT_TYPE = "pxl";
	/**
	 * GL "tracking" as an app for You-Again
	 */
	public static final String APP = "trk";	
	


	/**
	 * Make/retrieve a tracker cookie
	 * TODO move into YouAgain Client
	 * 
	 * <p>
	 * If do-not-track is set: this will *still* return a value
	 *  -- but the cookie is set to expire instantly.
	 * Use case: the server could report this in ajax json, and the client can decide whether or not
	 * to keep it for a session, or drop it for anonymity. Allows for the user to say "yes" a
	 * couple of interactions in (as with the AdUnit Consent CTA).
	 * </p>
	 * 
	 * @param state Can be null (returns null)
	 * @return A nonce@trk XId. 
	 * 
	 * Repeated calls will return the same uid. 
	 */
	public static String getCreateCookieTrackerId(WebRequest state) {
		if (state==null) return null;
		String uid = state.getCookie(trkid);
		if (uid!=null) {
			return uid;
		}
		uid = Utils.getRandomString(20)+"@trk";
		DataLogConfig dls = Dep.get(DataLogConfig.class);
		// Do not track?
		boolean dnt = state.isDoNotTrack();
		if (dnt) {
			// still set it (so repeated calls to this method within one server call return the same id)
			state.setCookie(trkid, uid, new Dt(0,TUnit.MILLISECOND), dls.COOKIE_DOMAIN);			
		} else {
			Dt expiry = TUnit.MONTH.dt;
			// Do we have explicit consent? -- see WebRequest.isDoNotTrack() as called above
			if ("C".equals(state.getResponse().getHeader("Tk"))) {
				expiry = TUnit.YEAR.dt;
			}
			state.setCookie(trkid, uid, expiry, dls.COOKIE_DOMAIN);
		}
		return uid;
	}
	
	/**
	 * retrieve a tracker cookie
	 * TODO move into YouAgain Client
	 * @param state Can be null (returns null)
	 * @return A nonce@trk XId. Can be null. 
	 */
	public static String getCookieTrackerId(WebRequest state) {
		if (state==null) return null;
		String uid = state.getCookie(trkid);
		return uid;
	}
	
	static File PIXEL = new File("web/img/tracking-pixel.gif");


	public void process(WebRequest state) {	
		// Who are they? Track them across pages
		String uid = getCreateCookieTrackerId(state);
		String ref = Utils.or(state.get("ref"), state.getReferer());
		// ??transfer link properties to cookie? -- like the affiliate
		// serve the resource, so we can release the request
		try {
			FileServlet.serveFile(PIXEL, state);
		} catch (IOException e) {
			Log.w("img0", e);
		}
		String tag = DATALOG_EVENT_TYPE;
		
		// Default to dataspace "trk" but allow override
		Dataspace dataspace = state.get(LgServlet.DATASPACE);
		if (dataspace == null) {
			dataspace = new Dataspace(APP);
		}
		String gby = state.get(LgServlet.GBY);
		// Count it
		LgServlet.doLog(state, dataspace, gby, tag, 1, null, null, true);

		
		// log it
		Log.d("track", state.getReferer()+" uid: "+uid);		
	}


	/**
	 * 
	 * @param state
	 * @return tracking-ID as an AuthToken or null
	 */
	public static AuthToken getAuthToken(WebRequest state) {
		String trkId = state.getCookie(TrackingPixelServlet.trkid);
		if (trkId == null) return null;
		String token = null;
		AuthToken ta = new AuthToken(token).setApp(APP).setXId(new XId(trkId, false));
		return ta;
	}


	
}
