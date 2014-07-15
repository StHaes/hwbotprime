package org.hwbot.opengl;

import java.util.List;

import org.hwbot.api.bench.dto.NotificationDTO;
import org.hwbot.opengl.tasks.CommentLoaderTask;
import org.hwbot.prime.api.CommentObserver;
import org.hwbot.prime.service.SecurityService;
import org.hwbot.prime.tasks.SubmitCommentTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;

public class CommentDialog extends DialogFragment {
    
	private String targetId;
	private String target;
	private View chatIcon;
	private TextSwitcher textSwitcher;
	private CommentObserver commentObserver;
	private List<NotificationDTO> notificationDTOs;

	// @Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
	}

	public List<NotificationDTO> getNotificationDTOs() {
		return notificationDTOs;
	}

	public void setNotificationDTOs(List<NotificationDTO> notificationDTOs) {
		this.notificationDTOs = notificationDTOs;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setChatIcon(View chatIcon) {
		this.chatIcon = chatIcon;
	}

	public void setTextSwitcher(TextSwitcher textSwitcher) {
		this.textSwitcher = textSwitcher;
	}

	public void setCommentObserver(CommentObserver commentObserver) {
		this.commentObserver = commentObserver;
	}

	// @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		final View view = inflater.inflate(R.layout.dialog_comment, null);
		LinearLayout commentBox = (LinearLayout) view.findViewById(R.id.commentBox);

		if (SecurityService.getInstance().isLoggedIn()) {
			builder.setView(view).setPositiveButton(R.string.comment_btn, new DialogInterface.OnClickListener() {
				// @Override
				public void onClick(DialogInterface dialog, int id) {
					EditText editText = (EditText) view.findViewById(R.id.commentInput);
					String text = editText.getText().toString();
					// Log.i(this.getClass().getSimpleName(), "Posting comment: " + text + " for target " + target + " / " + targetId);

					chatIcon.setAlpha(1.0f);

					// can we do network in this thread?
					if (org.apache.commons.lang.StringUtils.isNotEmpty(text)) {
						new SubmitCommentTask(text, targetId, target, chatIcon, textSwitcher, commentObserver).execute((Void) null);
					}
				}
			}).setNegativeButton(R.string.comment_cancel_btn, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					CommentDialog.this.getDialog().cancel();
					chatIcon.setAlpha(1.0f);
				}
			});
		} else {
			builder.setView(view).setNegativeButton(R.string.comment_not_loggedin_btn, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					CommentDialog.this.getDialog().cancel();
					chatIcon.setAlpha(1.0f);
				}
			});
		}

		new CommentLoaderTask(commentBox, target + "_" + targetId).execute((Void) null);

		return builder.create();
	}
}
