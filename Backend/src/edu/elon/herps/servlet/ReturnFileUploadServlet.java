package edu.elon.herps.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import org.apache.commons.io.IOUtils;

import org.json.simple.JSONObject;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;


//@SuppressWarnings("serial")
public class ReturnFileUploadServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		try {

			//get map of files (image and/or sound just uploaded to blobstore)
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);

			//create json object and assign blobs paths/keystrings as pairs
			JSONObject jsonResponse = new JSONObject();

			for (Map.Entry<String, List<BlobKey>> entry : blobs.entrySet()) {
				String blobKeyString = entry.getValue().get(0).getKeyString();
				jsonResponse.put(entry.getKey(), blobKeyString);
			}

			resp.setContentType("application/json");
			resp.getWriter().write(jsonResponse.toJSONString());

		} catch (Exception e) {
			throw new IOException(e);
		}
	}


	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getWriter().println("!");
	}
}
