package br.com.wjd.Classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import br.com.wjd.R;

public class AlertDialogCustom {

    public static void showDialog(Context context, String title, String message,
                                  DialogInterface.OnClickListener positiveListener,
                                  DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);

        // Inflate o layout personalizado
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_dialog_layout, null);

        // Configure o título e a mensagem
        TextView titleView = view.findViewById(R.id.dialog_title);
        TextView messageView = view.findViewById(R.id.dialog_message);
        titleView.setText(title);
        messageView.setText(message);

        builder.setView(view);

        if (positiveListener != null) {
            builder.setPositiveButton(R.string.ok, positiveListener);
        }

        if (negativeListener != null) {
            builder.setNegativeButton(R.string.no, negativeListener);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.rounded_dialog);
        }
    }
}
