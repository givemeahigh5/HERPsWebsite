package edu.elon.herps.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import javax.servlet.http.*;

import org.apache.commons.io.IOUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import edu.elon.herps.NameValuePair;


//@SuppressWarnings("serial")
public class UploadServletIOS extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		ServletFileUpload upload = new ServletFileUpload();

		try {
			if(ServletFileUpload.isMultipartContent(req)) {

				//initiate datastore and get iterator for formdata
				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				FileItemIterator items = upload.getItemIterator(req);

				String formName = "";
				LinkedList<NameValuePair> fields = new LinkedList<NameValuePair>();

				while(items.hasNext()) {
					FileItemStream item = items.next();
					InputStream stream = item.openStream();

					if(item.getFieldName().equals("form")) {
						formName = IOUtils.toString(stream, "UTF-8");

						//put field key/values pairs in list; added to entity later
						NameValuePair pair = new NameValuePair("Category", formName);
						fields.add(pair);

						NameValuePair submitTime = new NameValuePair("Submit Time", new Date());
						fields.add(submitTime);
					}
					else if(item.isFormField()) {
						//put field key/values pairs in list; added to entity later
						NameValuePair pair = new NameValuePair(item.getFieldName(), IOUtils.toString(stream, "UTF-8"));
						fields.add(pair);
					}
				}

				if(formName != "") {
					//check if table has entries; if not, insert record w/ slot order
					if(isEmptyCategory(formName, datastore)) {
						putOrderRecord(formName, datastore, fields);
					}

					Entity entity = new Entity(formName);

					for(int i=0; i < fields.size(); i++) {
						NameValuePair pair = fields.get(i);

						if(pair.name.equals("Submit Time")) {
							//set this property as a date object instead of string (has to happen before value cast to string for find)
							entity.setProperty(pair.name, pair.value);
						}
						else {
							String v = (String)pair.value;

							if(v.contains("BlobKey:")) {
								BlobKey blobKey = new BlobKey(v.replace("BlobKey:", ""));
								entity.setProperty(pair.name, blobKey);
							}
							else {
								entity.setProperty(pair.name, pair.value);
							}
						}
					}

					entity.setProperty("order", "false");
					datastore.put(entity);

					resp.getWriter().write("success");
				}
			}
			else {
				resp.setContentType("text/plain");
				resp.getWriter().write("fail");
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}



	private boolean isEmptyCategory(String formName, DatastoreService datastore) {
		Query q = new Query(formName);
		FetchOptions fo = FetchOptions.Builder.withLimit(1);
		int entries = datastore.prepare(q).countEntities(fo);

		if (entries == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	private void putOrderRecord(String formName, DatastoreService datastore, LinkedList<NameValuePair> fields) {
		if(formName != "") {
			Entity entity = new Entity(formName);

			for (int i = 0; i < fields.size(); i++) {
				NameValuePair pair = fields.get(i);
				entity.setProperty(pair.name, i);
			}

			entity.setProperty("order", "true");
			datastore.put(entity);
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getWriter().println("!");
	}
}
