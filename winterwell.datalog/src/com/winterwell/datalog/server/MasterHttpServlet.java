package com.winterwell.datalog.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.winterwell.utils.Key;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.web.WebUtils2;
import com.winterwell.web.WebEx;
import com.winterwell.web.app.AppUtils;
import com.winterwell.web.app.ManifestServlet;
import com.winterwell.web.app.WebRequest;

/**
 *
 */
public class MasterHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	public MasterHttpServlet() {
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		WebRequest request = null; // NB: If Eclipse says "resource leak" it's wrong -- the finally clause does it
		String path = null;
		try {
			Thread.currentThread().setName("web "+path);
			request = new WebRequest(null, req, resp);
			AppUtils.addDebugInfo(request);
			path = request.getRequestPath();
			Thread.currentThread().setName("web "+path);
	
			// Tracking pixel
			if (path.startsWith("/pxl")) {
				new TrackingPixelServlet().process(request);
				return;
			}		
			// Log
			if (path.startsWith("/lg")) {
				LgServlet.fastLog(request);
				return;
			}
			// No favicon ??and block other common requests??
			if (path.startsWith("/favicon")) {
				WebUtils2.sendError(404, "", resp);
				return;
			}
	
			// cors on
			if (DataLogServer.settings.CORS) {
				WebUtils2.CORS(request, true);
			}
			
			Log.d(request);
			
			// which dataspace?
			if (request.getSlug()==null) {
				throw new WebEx.E404(request.getRequestUrl(), "You must specify a project");
			}
			String project = request.getSlugBits()[0];
			request.put(new Key("project"), project);
			
			// data/stats explorer
			if (path.startsWith("/data")) {
				new DataServlet().process(request);
				return;
			}

			if (path.startsWith("/callback")) {
				new CallbackServlet().process(request);
				return;
			}

			// TODO experiment reports table
			
			// TODO experiment reports details
			
			// TODO dataspace admin
			
			if (path.equals("/ping")) {
				new PingServlet(request).doGet();
				return;
			}
			if (path.equals("/manifest")) {
				new ManifestServlet().process(request);
				return;
			}
			
			WebUtils2.sendError(500, "TODO", resp);
		} catch(Throwable ex) {
			// default to generic Server Error
			int errorCode = 500;
			// but catch and use code from web exceptions
			if (ex instanceof WebEx) {
				errorCode = ((WebEx) ex).code;
			}
			// and take it easy when logging 404 Not Found
			if (errorCode == 404) {
				Log.w("404", ex);	
			} else {
				Log.e("error", ex);
			}
			WebUtils2.sendError(errorCode, "Server Error: " + ex, resp);
		} finally {
			WebRequest.close(req, resp);
			Thread.currentThread().setName("done ...web "+path);
		}
	}

}
