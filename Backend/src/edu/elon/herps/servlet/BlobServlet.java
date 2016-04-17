package edu.elon.herps.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobInfo;


public class BlobServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		//initiate blobstore objects (blobinfofactory allows access to content type of blob)
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		BlobInfoFactory bif = new BlobInfoFactory();

		//get blob key based on id passed in
		BlobKey blobKey = new BlobKey(req.getParameter("id"));
		BlobInfo info = bif.loadBlobInfo(blobKey);

		//check content type; if application/octet-stream, it's a caf audio file
		if(info != null && info.getContentType().equals("application/octet-stream")) {
			resp.setHeader("Content-Disposition", "attachment; filename=\"recording.caf\"");
		}

		//serve blob as a web-displayed image or downloaded sound file
		blobstoreService.serve(blobKey, resp);
	}
}
