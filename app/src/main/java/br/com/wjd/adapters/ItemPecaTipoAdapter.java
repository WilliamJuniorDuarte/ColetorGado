package br.com.wjd.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.wjd.Classes.PecaTipo;
import br.com.wjd.R;

public class ItemPecaTipoAdapter extends RecyclerView.Adapter<ItemPecaTipoAdapter.MyViewHolder> {
    private List<PecaTipo> lista;
    private List<Integer> itensClicados = new ArrayList<>();

    public List<Integer> getcodigo(List<PecaTipo> lista){
        List<Integer> listcodigos = new ArrayList<>();
        for (int x = 0; x < itensClicados.size(); x++) {
            try {
                listcodigos.add(lista.get(itensClicados.get(x)).getCoditipo());
            } catch (NullPointerException e){
                listcodigos.add(0);
            }
        }
        return listcodigos;
    }

    public ItemPecaTipoAdapter(List<PecaTipo> lista) {
        if (lista.isEmpty()) {
            PecaTipo item = null;
            lista.add(item);
        }
        this.lista = lista;
    }

    @Override
    public void onBindViewHolder(ItemPecaTipoAdapter.MyViewHolder holder, int position) {
        PecaTipo item = lista.get(position);
        if (item != null) {
            holder.et_sigltipo.setText(item.getSigltipo());
            holder.et_nametipo.setText(item.getNametipo());
            holder.ck_select.setChecked(itensClicados.contains(holder.getAdapterPosition()));
            holder.ck_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSelection(holder.getAdapterPosition());
                }
            });
            holder.vw_color.setBackgroundColor(Color.parseColor(item.getColotipo()));
        } else {
            holder.et_nametipo.setText(holder.itemView.getResources().getString(R.string.no_row_data));
            holder.ck_select.setVisibility(View.INVISIBLE);
            holder.et_sigltipo.setVisibility(View.INVISIBLE);
        }
    }

    public void selectCodiTipos(String selecteds){
        for (int x = 0; x < lista.size(); x++) {
            if (selecteds.contains(lista.get(x).getCoditipo()+","))
                itensClicados.add(x);
        }
    }

    public void toggleSelection(int position) {
        if (itensClicados.contains(position)) {
            itensClicados.remove(Integer.valueOf(position));
        } else {
            itensClicados.add(position);
        }
        notifyDataSetChanged();
    }

    @Override
    public ItemPecaTipoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listpecatipo, parent, false);
        return new ItemPecaTipoAdapter.MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView et_sigltipo, et_nametipo;
        public CheckBox ck_select;
        public View vw_color;

        public MyViewHolder(View view) {
            super(view);
            et_sigltipo = view.findViewById(R.id.et_sigl_listPecaTip);
            et_nametipo = view.findViewById(R.id.et_name_listPecaTip);
            ck_select = view.findViewById(R.id.ck_select_listPecaTipo);
            vw_color = view.findViewById(R.id.vw_color_listPecaTipo);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSelection(getAdapterPosition());
                }
            });
        }
    }
}


