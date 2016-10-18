package com.infinite.weixincircle.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;


import java.lang.ref.WeakReference;


/**
 * Created by Administrator on 2016/4/11.
 */
@SuppressLint("ValidFragment")
public class MyDialog extends DialogFragment {
    private int icon_id;
    private String title;
    private String message;
    private String postive;
    private String nagtive;
    private String[] items;
    private boolean cancelable;
    private CustomDialogClickListener listener;
    private Builder mBuilder;

    /**
     * @param title
     * @param message
     * @param postive
     * @param nagtive
     * @param cancelable
     * @param listener
     */
    public MyDialog(String title, String message, String postive, String nagtive, boolean cancelable, CustomDialogClickListener listener) {
        this(-1, title, message, null, postive, nagtive, cancelable, listener);
    }

    /**
     * 在Dialog中添加item
     *
     * @param title
     * @param message
     * @param items
     * @param postive
     * @param nagtive
     * @param cancelable
     * @param listener
     */
    public MyDialog(String title, String message, String[] items, String postive, String nagtive, boolean cancelable, CustomDialogClickListener listener) {
        this(-1, title, message, items, postive, nagtive, cancelable, listener);
    }


    /**
     * @param icon_id
     * @param title
     * @param message
     * @param postive
     * @param nagtive
     * @param cancelable
     * @param listener
     */

    public MyDialog(int icon_id, String title, String message, String[] items, String postive, String nagtive, boolean cancelable, CustomDialogClickListener listener) {
        this.icon_id = icon_id;
        this.title = title;
        this.message = message;
        this.items = items;
        this.postive = postive;
        this.nagtive = nagtive;
        this.cancelable = cancelable;
        this.listener = listener;
    }

    public MyDialog(Builder builder) {
        this.mBuilder = builder;
        this.setCancelable(builder.cancelable);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mBuilder == null) {
            mBuilder = new Builder(getActivity());
            mBuilder.setIconId(icon_id);
            mBuilder.setTitle(title);
            mBuilder.setMessage(message);
            mBuilder.setItems(items);
            mBuilder.setPostive(postive);
            mBuilder.setNagtive(nagtive);
            mBuilder.setOnClickListener(listener);
            this.setCancelable(cancelable);
        }
        return mBuilder.create();

    }

    public static class Builder {
        private int icon_id = -1;
        private int style = -1;
        private Object view;
        private String title;
        private String message;
        private String postive;
        private String nagtive;
        private String[] items;
        private boolean cancelable = true;
        private CustomDialogClickListener listener;
        private WeakReference<Activity> mContext;

        public Builder(Activity context) {
            mContext = new WeakReference<>(context);
        }

        public Builder setIconId(int icon_id) {
            this.icon_id = icon_id;
            return this;
        }

        public Builder setStyle(@StyleRes int style) {
            this.style = style;
            return this;
        }

        public Builder setView(Object view) {
            this.view = view;
            return this;
        }

        public Builder setTitle(Object title) {
            if (title instanceof String) {
                this.title = (String) title;
            }
            if (title instanceof Integer) {
                this.title = mContext.get().getString((Integer) title);
            }
            return this;
        }

        public Builder setMessage(Object message) {
            if (message instanceof String) {
                this.message = (String) message;
            }
            if (message instanceof Integer) {
                this.message = mContext.get().getString((Integer) message);
            }
            return this;
        }

        public Builder setPostive(Object postive) {
            if (postive instanceof String) {
                this.postive = (String) postive;
            }
            if (postive instanceof Integer) {
                this.postive = mContext.get().getString((Integer) postive);
            }
            return this;
        }

        public Builder setNagtive(Object nagtive) {
            if (nagtive instanceof String) {
                this.nagtive = (String) nagtive;
            }
            if (nagtive instanceof Integer) {
                this.nagtive = mContext.get().getString((Integer) nagtive);
            }
            return this;
        }

        public Builder setItems(String[] items) {
            this.items = items;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setOnClickListener(CustomDialogClickListener listener) {
            this.listener = listener;
            return this;
        }

        public AlertDialog create() {
            Activity activity = mContext.get();
            if (activity != null) {
                AlertDialog.Builder builder;
                if (style != -1) {
                    builder = new AlertDialog.Builder(activity, style);
                } else {
                    builder = new AlertDialog.Builder(activity);
                }

                if (icon_id != -1) {
                    builder.setIcon(icon_id);
                }

                if (view != null) {
                    if (view instanceof Integer) {
                        builder.setView((Integer) view);
                    }
                    if (view instanceof View) {
                        builder.setView((View) view);
                    }
                }

                if (!TextUtils.isEmpty(title)) {
                    builder.setTitle(title);
                }
                if (!TextUtils.isEmpty(message)) {
                    builder.setMessage(message);
                }

                if (items != null && items.length != 0) {
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listener != null) {
                                listener.onItemClick(dialog, which);
                            }
                        }
                    });
                }

                if (!TextUtils.isEmpty(postive)) {
                    builder.setPositiveButton(postive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (null != listener) {
                                listener.onPostiveClick(dialog, which);
                            }
                        }
                    });
                }

                if (!TextUtils.isEmpty(nagtive)) {
                    builder.setNegativeButton(nagtive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (null != listener) {
                                listener.onNagetiveClick(dialog, which);
                            }
                        }
                    });
                }
                return builder.create();
            }
            return null;
        }
    }

}

