package net.osmand.plus.settings.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import net.osmand.AndroidUtils;
import net.osmand.FileUtils;
import net.osmand.IndexConstants;
import net.osmand.PlatformUtil;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithCompoundButton;
import net.osmand.plus.base.bottomsheetmenu.SimpleBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.settings.backend.ExportSettingsType;
import net.osmand.plus.settings.backend.ApplicationMode;
import net.osmand.plus.settings.backend.backup.SettingsHelper;
import net.osmand.plus.settings.backend.backup.ProfileSettingsItem;
import net.osmand.plus.settings.backend.backup.SettingsItem;
import net.osmand.plus.settings.bottomsheets.BasePreferenceBottomSheet;

import org.apache.commons.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportProfileBottomSheet extends BasePreferenceBottomSheet {

	public static final String TAG = ExportProfileBottomSheet.class.getSimpleName();

	private static final Log LOG = PlatformUtil.getLog(ExportProfileBottomSheet.class);

	private static final String INCLUDE_ADDITIONAL_DATA_KEY = "INCLUDE_ADDITIONAL_DATA_KEY";
	private static final String EXPORTING_PROFILE_KEY = "exporting_profile_key";

	private OsmandApplication app;
	private ApplicationMode profile;
	private Map<ExportSettingsType, List<?>> dataList = new HashMap<>();
	private ExportImportSettingsAdapter adapter;

	private SettingsHelper.SettingsExportListener exportListener;
	private ProgressDialog progress;

	private boolean includeAdditionalData = false;
	private boolean exportingProfile = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = requiredMyApplication();
		profile = getAppMode();
		dataList = app.getSettingsHelper().getAdditionalData();
		if (savedInstanceState != null) {
			includeAdditionalData = savedInstanceState.getBoolean(INCLUDE_ADDITIONAL_DATA_KEY);
			exportingProfile = savedInstanceState.getBoolean(EXPORTING_PROFILE_KEY);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INCLUDE_ADDITIONAL_DATA_KEY, includeAdditionalData);
		outState.putBoolean(EXPORTING_PROFILE_KEY, exportingProfile);
	}

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		final Context context = getContext();
		if (context == null) {
			return;
		}
		LayoutInflater inflater = UiUtilities.getInflater(app, nightMode);

		int profileColor = profile.getIconColorInfo().getColor(nightMode);
		int colorNoAlpha = ContextCompat.getColor(context, profileColor);

		Drawable backgroundIcon = UiUtilities.getColoredSelectableDrawable(context, colorNoAlpha, 0.3f);
		Drawable[] layers = {new ColorDrawable(UiUtilities.getColorWithAlpha(colorNoAlpha, 0.10f)), backgroundIcon};

		items.add(new TitleItem(getString(R.string.export_profile)));

		BaseBottomSheetItem profileItem = new BottomSheetItemWithCompoundButton.Builder()
				.setChecked(true)
				.setCompoundButtonColorId(profileColor)
				.setButtonTintList(ColorStateList.valueOf(getResolvedColor(profileColor)))
				.setDescription(BaseSettingsFragment.getAppModeDescription(context, profile))
				.setIcon(getIcon(profile.getIconRes(), profileColor))
				.setTitle(profile.toHumanString())
				.setBackground(new LayerDrawable(layers))
				.setLayoutId(R.layout.preference_profile_item_with_radio_btn)
				.create();
		items.add(profileItem);

		if (!dataList.isEmpty()) {
			final View additionalDataView = inflater.inflate(R.layout.bottom_sheet_item_additional_data, null);
			ExpandableListView listView = additionalDataView.findViewById(R.id.list);
			adapter = new ExportImportSettingsAdapter(app, nightMode, false);
			View listHeader = inflater.inflate(R.layout.item_header_export_expand_list, null);
			final View topSwitchDivider = listHeader.findViewById(R.id.topSwitchDivider);
			final View bottomSwitchDivider = listHeader.findViewById(R.id.bottomSwitchDivider);
			final SwitchCompat switchItem = listHeader.findViewById(R.id.switchItem);
			switchItem.setTextColor(getResources().getColor(nightMode ? R.color.active_color_primary_dark : R.color.active_color_primary_light));
			switchItem.setChecked(includeAdditionalData);
			switchItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					includeAdditionalData = !includeAdditionalData;
					topSwitchDivider.setVisibility(includeAdditionalData ? View.VISIBLE : View.GONE);
					bottomSwitchDivider.setVisibility(includeAdditionalData ? View.VISIBLE : View.GONE);
					if (includeAdditionalData) {
						adapter.updateSettingsList(app.getSettingsHelper().getAdditionalData());
						adapter.selectAll(true);
					} else {
						adapter.selectAll(false);
						adapter.clearSettingsList();
					}
					updateSwitch(switchItem);
					setupHeightAndBackground(getView());
				}
			});
			listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
				@Override
				public void onGroupExpand(int i) {
					setupHeightAndBackground(getView());
				}
			});

			updateSwitch(switchItem);
			listView.addHeaderView(listHeader);
			listView.setAdapter(adapter);
			final SimpleBottomSheetItem titleItem = (SimpleBottomSheetItem) new SimpleBottomSheetItem.Builder()
					.setCustomView(additionalDataView)
					.create();
			items.add(titleItem);
		}
	}

	private void updateSwitch(View view) {
		if (includeAdditionalData) {
			UiUtilities.setMargins(view, 0, 0, 0, 0);
			view.setPadding(AndroidUtils.dpToPx(app, 32), 0, AndroidUtils.dpToPx(app, 32), 0);
		} else {
			UiUtilities.setMargins(view, AndroidUtils.dpToPx(app, 16), 0, AndroidUtils.dpToPx(app, 16), 0);
			view.setPadding(AndroidUtils.dpToPx(app, 16), 0, AndroidUtils.dpToPx(app, 16), 0);
		}
	}

	@Override
	protected int getRightBottomButtonTextId() {
		return R.string.shared_string_export;
	}

	@Override
	protected void onRightBottomButtonClick() {
		prepareFile();
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_cancel;
	}

	@Override
	protected boolean useScrollableItemsContainer() {
		return false;
	}

	@Override
	protected boolean useExpandableList() {
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		checkExportingFile();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (exportingProfile) {
			File file = getExportFile();
			app.getSettingsHelper().updateExportListener(file, null);
		}
	}

	private List<SettingsItem> prepareSettingsItemsForExport() {
		List<SettingsItem> settingsItems = new ArrayList<>();
		settingsItems.add(new ProfileSettingsItem(app, profile));
		if (includeAdditionalData) {
			settingsItems.addAll(app.getSettingsHelper().prepareAdditionalSettingsItems(adapter.getData()));
		}
		return settingsItems;
	}

	private void prepareFile() {
		if (app != null) {
			exportingProfile = true;
			showExportProgressDialog();
			File tempDir = FileUtils.getTempDir(app);
			String fileName = profile.toHumanString();
			app.getSettingsHelper().exportSettings(tempDir, fileName, getSettingsExportListener(), prepareSettingsItemsForExport(), true);
		}
	}

	private void showExportProgressDialog() {
		Context context = getContext();
		if (context == null) {
			return;
		}
		if (progress != null) {
			progress.dismiss();
		}
		progress = new ProgressDialog(context);
		progress.setTitle(app.getString(R.string.export_profile));
		progress.setMessage(app.getString(R.string.shared_string_preparing));
		progress.setCancelable(false);
		progress.show();
	}

	private SettingsHelper.SettingsExportListener getSettingsExportListener() {
		if (exportListener == null) {
			exportListener = new SettingsHelper.SettingsExportListener() {

				@Override
				public void onSettingsExportFinished(@NonNull File file, boolean succeed) {
					dismissExportProgressDialog();
					exportingProfile = false;
					if (succeed) {
						shareProfile(file, profile);
					} else {
						app.showToastMessage(R.string.export_profile_failed);
					}
				}
			};
		}
		return exportListener;
	}

	private void checkExportingFile() {
		if (exportingProfile) {
			File file = getExportFile();
			boolean fileExporting = app.getSettingsHelper().isFileExporting(file);
			if (fileExporting) {
				showExportProgressDialog();
				app.getSettingsHelper().updateExportListener(file, getSettingsExportListener());
			} else if (file.exists()) {
				dismissExportProgressDialog();
				shareProfile(file, profile);
			}
		}
	}

	private void dismissExportProgressDialog() {
		FragmentActivity activity = getActivity();
		if (progress != null && activity != null && AndroidUtils.isActivityNotDestroyed(activity)) {
			progress.dismiss();
		}
	}

	private File getExportFile() {
		File tempDir = FileUtils.getTempDir(app);
		String fileName = profile.toHumanString();
		return new File(tempDir, fileName + IndexConstants.OSMAND_SETTINGS_FILE_EXT);
	}

	private void shareProfile(@NonNull File file, @NonNull ApplicationMode profile) {
		try {
			final Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, profile.toHumanString() + IndexConstants.OSMAND_SETTINGS_FILE_EXT);
			sendIntent.putExtra(Intent.EXTRA_STREAM, AndroidUtils.getUriForFile(getMyApplication(), file));
			sendIntent.setType("*/*");
			sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(sendIntent);
			dismiss();
		} catch (Exception e) {
			Toast.makeText(requireContext(), R.string.export_profile_failed, Toast.LENGTH_SHORT).show();
			LOG.error("Share profile error", e);
		}
	}

	public static boolean showInstance(@NonNull FragmentManager fragmentManager,
									   Fragment target,
									   @NonNull ApplicationMode appMode) {
		try {
			ExportProfileBottomSheet fragment = new ExportProfileBottomSheet();
			fragment.setAppMode(appMode);
			fragment.setTargetFragment(target, 0);
			fragment.show(fragmentManager, TAG);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}
}
