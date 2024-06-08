package br.com.wjd.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.wjd.Classes.PecaClas;
import br.com.wjd.R;

public class ItemPecaClasAdapter  extends RecyclerView.Adapter<ItemPecaClasAdapter.MyViewHolder>{

    private List<PecaClas> lista;
    private PecaClas item;

    public ItemPecaClasAdapter(List<PecaClas> lista) {
        if (lista.isEmpty()) {
            item = new PecaClas();
            item.setCodiclas(0);
            lista.add(item);
        }
        this.item = null;
        this.lista = lista;
    }

    @Override
    public void onBindViewHolder(ItemPecaClasAdapter.MyViewHolder holder, int position) {
        item = lista.get(position);
        if (item.getCodiclas() > 0) {
            holder.tv_name.setText(item.getNomeclas());
        } else {
            holder.tv_name.setText(holder.itemView.getResources().getString(R.string.no_row_data));
            holder.edit.setVisibility(ViewGroup.INVISIBLE);
        }
    }

    @Override
    public ItemPecaClasAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listpecaclas, parent, false);
        return new ItemPecaClasAdapter.MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_name;
        public ImageButton edit;

        public MyViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.textView_item_listPecaClas);
            edit = view.findViewById(R.id.imageButton_item_listPecaClas);
        }
    }
}
