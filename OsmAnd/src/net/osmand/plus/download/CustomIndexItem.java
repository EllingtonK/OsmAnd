package net.osmand.plus.download;

import android.content.Context;

import androidx.annotation.NonNull;

import net.osmand.JsonUtils;
import net.osmand.map.OsmandRegions;
import net.osmand.plus.OsmandApplication;
import net.osmand.util.Algorithms;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CustomIndexItem extends IndexItem {

	private String subfolder;
	private String downloadUrl;
	private String webUrl;

	private List<String> imageDescrUrl;
	private Map<String, String> names;
	private Map<String, String> descriptions;
	private Map<String, String> webButtonTexts;

	public CustomIndexItem(String fileName,
	                       String subfolder,
	                       String downloadUrl,
	                       String webUrl,
	                       String size,
	                       long timestamp,
	                       long contentSize,
	                       long containerSize,
	                       List<String> imageDescrUrl,
	                       Map<String, String> names,
	                       Map<String, String> descriptions,
	                       Map<String, String> webButtonTexts,
	                       @NonNull DownloadActivityType type) {
		super(fileName, null, timestamp, size, contentSize, containerSize, type);
		this.subfolder = subfolder;
		this.downloadUrl = downloadUrl;
		this.webUrl = webUrl;
		this.imageDescrUrl = imageDescrUrl;
		this.names = names;
		this.descriptions = descriptions;
		this.webButtonTexts = webButtonTexts;
	}

	@Override
	public DownloadEntry createDownloadEntry(OsmandApplication ctx) {
		DownloadEntry entry = super.createDownloadEntry(ctx);
		if (entry != null) {
			entry.urlToDownload = downloadUrl;
		}
		return entry;
	}

	@Override
	public File getTargetFile(OsmandApplication ctx) {
		String basename = getTranslatedBasename();
		if (!Algorithms.isEmpty(subfolder)) {
			basename = subfolder + "/" + basename;
		}
		return new File(type.getDownloadFolder(ctx, this), basename + type.getUnzipExtension(ctx, this));
	}

	@Override
	public String getVisibleName(Context ctx, OsmandRegions osmandRegions) {
		return getVisibleName(ctx, osmandRegions, true);
	}

	@Override
	public String getVisibleName(Context ctx, OsmandRegions osmandRegions, boolean includingParent) {
		String name = super.getVisibleName(ctx, osmandRegions, includingParent);
		return JsonUtils.getLocalizedResFromMap(ctx, names, name);
	}

	public List<String> getDescriptionImageUrl() {
		return imageDescrUrl;
	}

	public String getLocalizedDescription(Context ctx) {
		String description = super.getDescription();
		return JsonUtils.getLocalizedResFromMap(ctx, descriptions, description);
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getWebButtonText(Context ctx) {
		return JsonUtils.getLocalizedResFromMap(ctx, webButtonTexts, null);
	}

	public static class CustomIndexItemBuilder {

		private String fileName;
		private String subfolder;
		private String downloadUrl;
		private String webUrl;
		private String size;

		private long timestamp;
		private long contentSize;
		private long containerSize;

		private List<String> imageDescrUrl;
		private Map<String, String> names;
		private Map<String, String> descriptions;
		private Map<String, String> webButtonText;
		private DownloadActivityType type;


		public CustomIndexItemBuilder setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		public CustomIndexItemBuilder setSubfolder(String subfolder) {
			this.subfolder = subfolder;
			return this;
		}

		public CustomIndexItemBuilder setDownloadUrl(String downloadUrl) {
			this.downloadUrl = downloadUrl;
			return this;
		}

		public CustomIndexItemBuilder setWebUrl(String webUrl) {
			this.webUrl = webUrl;
			return this;
		}

		public CustomIndexItemBuilder setSize(String size) {
			this.size = size;
			return this;
		}

		public CustomIndexItemBuilder setTimestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public CustomIndexItemBuilder setContentSize(long contentSize) {
			this.contentSize = contentSize;
			return this;
		}

		public CustomIndexItemBuilder setContainerSize(long containerSize) {
			this.containerSize = containerSize;
			return this;
		}

		public CustomIndexItemBuilder setImageDescrUrl(List<String> imageDescrUrl) {
			this.imageDescrUrl = imageDescrUrl;
			return this;
		}

		public CustomIndexItemBuilder setNames(Map<String, String> names) {
			this.names = names;
			return this;
		}

		public CustomIndexItemBuilder setDescriptions(Map<String, String> descriptions) {
			this.descriptions = descriptions;
			return this;
		}

		public CustomIndexItemBuilder setWebButtonText(Map<String, String> webButtonText) {
			this.webButtonText = webButtonText;
			return this;
		}

		public CustomIndexItemBuilder setType(@NonNull DownloadActivityType type) {
			this.type = type;
			return this;
		}

		public CustomIndexItem create() {
			return new CustomIndexItem(fileName,
					subfolder,
					downloadUrl,
					webUrl,
					size,
					timestamp,
					contentSize,
					containerSize,
					imageDescrUrl,
					names,
					descriptions,
					webButtonText,
					type);
		}
	}
}