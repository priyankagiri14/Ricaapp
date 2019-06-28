package com.ubits.payflow.payflow_network.Driver.Stock_allocate;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.ubits.payflow.payflow_network.R;
import com.ubits.payflow.payflow_network.adapter.ProcessBatchnAdapter;

import java.util.ArrayList;
import java.util.List;

public class Assign_agent extends AppCompatActivity {

    private Toolbar toolbar;
    private int selectedPosition = -1;
    ViewHolder holder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assign_agent);
        toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("Agent name with id");
        setSupportActionBar(toolbar);

        setTitle("dev2qa.com - Android ListView With CheckBox");

        // Get listview checkbox.
        final ListView listViewWithCheckbox = (ListView)findViewById(R.id.listview_with_checkbox);

        // Initiate listview data.
        final List<ListViewItemDTO> initItemList = this.getInitViewItemDtoList();
        holder= (ViewHolder) listViewWithCheckbox.getTag();
        listViewWithCheckbox.setTag(holder);

        holder.checkBox.setOnClickListener(onStateChangedListener(holder.checkBox,position));

        // Create a custom list view adapter with checkbox control.
        final ListViewItemCheckboxBaseAdapter listViewDataAdapter = new ListViewItemCheckboxBaseAdapter(getApplicationContext(), initItemList);

        listViewDataAdapter.notifyDataSetChanged();

        // Set data adapter to list view.
        listViewWithCheckbox.setAdapter(listViewDataAdapter);

        // When list view item is clicked.
       /* listViewWithCheckbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long l) {
                // Get user selected item.
                Object itemObject = adapterView.getAdapter().getItem(itemIndex);

                // Translate the selected item to DTO object.
                ListViewItemDTO itemDto = (ListViewItemDTO)itemObject;

                // Get the checkbox.
                CheckBox itemCheckbox = (CheckBox) view.findViewById(R.id.list_view_item_checkbox);

                // Reverse the checkbox and clicked item check state.
                if(itemDto.isChecked())
                {
                    itemCheckbox.setChecked(false);
                    itemDto.setChecked(false);
                }else
                {
                    itemCheckbox.setChecked(true);
                    itemDto.setChecked(true);
                }

                //Toast.makeText(getApplicationContext(), "select item text : " + itemDto.getItemText(), Toast.LENGTH_SHORT).show();
            }
        });*/




        Button selectRemoveButton = (Button)findViewById(R.id.send);
        selectRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog alertDialog = new AlertDialog.Builder(Assign_agent.this).create();
                alertDialog.setMessage("Are you want to send this stock to the agent");

                alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        int size = initItemList.size();
                        for(int i=0;i<size;i++)
                        {
                            ListViewItemDTO dto = initItemList.get(i);

                            if(dto.isChecked())
                            {
                           /* initItemList.remove(i);
                            i--;
                            size = initItemList.size();*/
                                Toast.makeText(getApplicationContext(),""+initItemList.get(i).getItemText(),Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if(!dto.isChecked()){
                                Toast.makeText(getApplicationContext(),"Select any lot",Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }

                        listViewDataAdapter.notifyDataSetChanged();
                    }
                });

                alertDialog.show();
            }
        });

    }

    private View.OnClickListener onStateChangedListener(CheckBox checkBox,int position){

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()){
                    selectedPosition=position;
                }
                else{
                    selectedPosition=-1;
                }
                notifyDataSetChanged();
            }
        };
    }

    // Return an initialize list of ListViewItemDTO.
    private List<ListViewItemDTO> getInitViewItemDtoList()
    {
        String itemTextArr[] = {"Aman (123)", "Mohit (234)", "Ajay (454)", "Sachin (555)", "Mayank (456)", "Ojas (567)", "Arjun (888)"};

        List<ListViewItemDTO> ret = new ArrayList<ListViewItemDTO>();

        int length = itemTextArr.length;

        for(int i=0;i<length;i++)
        {
            String itemText = itemTextArr[i];

            ListViewItemDTO dto = new ListViewItemDTO();
            dto.setChecked(false);
            dto.setItemText(itemText);

            ret.add(dto);
        }

        return ret;
    }

    private static class ViewHolder {
        private TextView friendName;
        private CheckBox checkBox;

        @SuppressLint("WrongViewCast")
        public ViewHolder(View v) {
            checkBox = (CheckBox) v.findViewById(R.id.listview_with_checkbox);

    }
}
}
