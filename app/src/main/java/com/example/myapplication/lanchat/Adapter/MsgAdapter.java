package com.example.myapplication.lanchat.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.lanchat.Bean.Msg;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MsgAdapter extends ArrayAdapter<Msg> {
    private static final String TAG = "MsgAdapter";
    private int resourceId;
    private Context mContext;
    LayoutInflater inflater;

    public MsgAdapter(@NonNull Context context, int resource, @NonNull List<Msg> objects) {
        super(context, resource, objects);
        resourceId = resource;
        mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Msg msg = getItem(position);
        View view;
        ViewHolder viewHolder;

        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.firstLayout=view.findViewById(R.id.first_layout);
            viewHolder.secondLayout=view.findViewById(R.id.second_layout);
            viewHolder.riImageLeft=view .findViewById(R.id.ri_image_left);
            viewHolder.riImageRight=view .findViewById(R.id.ri_image_right);
            viewHolder.leftLayout = view.findViewById(R.id.left_layout);
            viewHolder.rightLayout = view.findViewById(R.id.right_Layout);
            viewHolder.leftMsg = view.findViewById(R.id.left_msg);
            viewHolder.rightMsg = view.findViewById(R.id.right_msg);
            viewHolder.leftImage = view.findViewById(R.id.left_image);
            viewHolder.rightImage = view.findViewById(R.id.right_image);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if(msg.getType()==Msg.RECEIVED){
            Log.d("2", "TCP客户端接收消息   getView: MsgAdapter收到消息");
            //如果是收到的消息，则显示左边消息布局，将右边消息布局隐藏
            if(msg.getContentType()==Msg.TEXT){
                viewHolder.firstLayout.setVisibility(View.VISIBLE);
                viewHolder.secondLayout.setVisibility(View.GONE);

                viewHolder.leftLayout.setVisibility(View.VISIBLE);
                viewHolder.rightLayout.setVisibility(View.GONE);

                viewHolder.leftImage.setVisibility(View.GONE);
                viewHolder.rightImage.setVisibility(View.GONE);

                viewHolder.riImageLeft.setVisibility(View.VISIBLE);
                viewHolder.riImageRight.setVisibility(View.GONE);

                viewHolder.leftMsg.setVisibility(View.VISIBLE);
                viewHolder.rightMsg.setVisibility(View.GONE);

                viewHolder.leftMsg.setText(msg.getContent());
                Log.d(TAG, "TCP客户端接收消息   getView:= "+msg.getContent());
                Log.d("2", "TCP客户端接收消息   getView: MsgAdapter布局成功");
            }
            else if(msg.getContentType()==Msg.IMAGE){
                Log.d("2", "getView: 收到图片"+(msg.getImageBitmap()!=null));
                viewHolder.firstLayout.setVisibility(View.VISIBLE);
                viewHolder.secondLayout.setVisibility(View.GONE);

                viewHolder.leftLayout.setVisibility(View.GONE);
                viewHolder.rightLayout.setVisibility(View.GONE);

                viewHolder.leftImage.setVisibility(View.VISIBLE);
                viewHolder.rightImage.setVisibility(View.GONE);

                viewHolder.leftMsg.setVisibility(View.GONE);
                viewHolder.rightMsg.setVisibility(View.GONE);

                viewHolder.riImageLeft.setVisibility(View.VISIBLE);
                viewHolder.riImageRight.setVisibility(View.GONE);
                viewHolder.leftImage.setImageBitmap(msg.getImageBitmap());
            }
            return view;
        }else if(msg.getType()==Msg.SENT){

            //如果是发出去的消息，显示右边布局的消息布局，将左边的消息布局隐藏
            if(msg.getContentType()==Msg.TEXT && msg.getImageUri()==null){
                Log.d("2", "getView: 来了");

                viewHolder.secondLayout.setVisibility(View.VISIBLE);
                viewHolder.firstLayout.setVisibility(View.GONE);

                viewHolder.rightImage.setVisibility(View.GONE);
                viewHolder.leftImage.setVisibility(View.GONE);

                viewHolder.riImageLeft.setVisibility(View.GONE);
                viewHolder.riImageRight.setVisibility(View.VISIBLE);

                viewHolder.rightLayout.setVisibility(View.VISIBLE);
                viewHolder.leftLayout.setVisibility(View.GONE);

                viewHolder.rightMsg.setVisibility(View.VISIBLE);
                viewHolder.leftMsg.setVisibility(View.GONE);
                viewHolder.rightMsg.setText(msg.getContent());
            }else if(msg.getContentType()==Msg.IMAGE && msg.getContent()==null){

                viewHolder.secondLayout.setVisibility(View.VISIBLE);
                viewHolder.firstLayout.setVisibility(View.GONE);

                viewHolder.rightImage.setVisibility(View.VISIBLE);
                viewHolder.leftImage.setVisibility(View.GONE);

                viewHolder.rightMsg.setVisibility(View.GONE);
                viewHolder.leftMsg.setVisibility(View.GONE);

                viewHolder.rightLayout.setVisibility(View.GONE);
                viewHolder.leftLayout.setVisibility(View.VISIBLE);

                viewHolder.riImageLeft.setVisibility(View.GONE);
                viewHolder.riImageRight.setVisibility(View.VISIBLE);
                viewHolder.rightImage.setImageURI(msg.getImageUri());
            }
            return view;
        }

        return view;
    }

    /**
     * ViewHolder内部类，避免UI组件重复加载
     */
    class ViewHolder{
        RelativeLayout firstLayout;
        RelativeLayout secondLayout;
        CircleImageView riImageLeft;
        CircleImageView riImageRight;
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        RoundedImageView leftImage;
        RoundedImageView rightImage;


    }
}
