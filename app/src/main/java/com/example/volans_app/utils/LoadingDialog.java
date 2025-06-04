package com.example.volans_app.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.volans_app.R;

public class LoadingDialog {
    private Dialog dialog;
    private TextView tvLoadingMessage;
    private ImageView iconQuiz;
    private Context context;
    private Animation rotateAnimation;

    public LoadingDialog(Context context) {
        this.context = context;
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_loading_quiz, null);
        dialog.setContentView(dialogView);

        // Configurar janela do diálogo
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        // Inicializar views
        tvLoadingMessage = dialogView.findViewById(R.id.tvLoadingMessage);
        iconQuiz = dialogView.findViewById(R.id.iconQuiz);

        // Criar animação de rotação
        rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_loading);

        // Configurar diálogo
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void show(String message) {
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
        }

        // Iniciar animação de rotação
        if (iconQuiz != null && rotateAnimation != null) {
            iconQuiz.startAnimation(rotateAnimation);
        }

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void updateMessage(String message) {
        if (tvLoadingMessage != null && dialog != null && dialog.isShowing()) {
            tvLoadingMessage.setText(message);
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            // Parar animação antes de fechar
            if (iconQuiz != null) {
                iconQuiz.clearAnimation();
            }
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void setMessage(String message) {
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
        }
    }
}