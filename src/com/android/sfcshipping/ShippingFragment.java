package com.android.sfcshipping;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.Integer;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import com.android.util.HttpUtil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.zxing.activity.CaptureActivity;


public class ShippingFragment extends Fragment {

	private Spinner sp;
	private List<String> list = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private TextView emp_no;
	private TextView dn_no;
	private TextView model_name;
	private TextView ship_qty;
	private Button shipButton;
	private ImageButton dnButton;
	private ImageButton dnScanButton;
	private ListView scanlist;
	private Button login;
	public TextView unit_list;
	public TextView unit_qty;
	public Button scanButton;
	public static final int SCAN_CODE=99;
	public MyAdapter myAdapter;
	public String result;
	public TextView total_qty;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	private String login(String emp) {
		// TODO Auto-generated method stub
		return query(emp);
	}

	private String query(String emp) {
		// TODO Auto-generated method stub
		String queryString = "emp="+emp;		
		String url = HttpUtil.BASE_URL+"LoginServlet?"+queryString;	
		return HttpUtil.queryStringForPost(url);
	}

	private String queryDN(String dn)
	{
		String queryString="dn="+dn;
		String url=HttpUtil.BASE_URL+"DnServlet?"+queryString;
		return HttpUtil.queryStringForPost(url);
	}
	private String queryQty(String model,String unit)
	{
		String queryString="model_name="+model+"&unit="+unit;
		String url=HttpUtil.BASE_URL+"QtyServlet?"+queryString;
		return HttpUtil.queryStringForPost(url);
	}
	private String ship(String emp,String list,String qty,String dn)
	{
		String queryString="emp="+emp+"&unit_list="+list+"&qty="+qty+"&dn="+dn;
		String url=HttpUtil.BASE_URL+"ShipServlet?"+queryString;
		return HttpUtil.queryStringForPost(url);
	}
	private boolean validate(String emp) {
		// TODO Auto-generated method stub
		if(emp.equals("")){
			return false;
		}	
		return true;
	}
	
	private String splitStr(String str)
	{
		String []stemp=str.split("\\|");
		return stemp[0];
	}
	
	public boolean isNetworkConnected(Context context)
	{
		try
		{
			ConnectivityManager mConnectivityManager=(ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			if(mConnectivityManager==null)
				return false;
			NetworkInfo mNetworkInfo=mConnectivityManager.getActiveNetworkInfo();
			if(mNetworkInfo==null||!mNetworkInfo.isAvailable()||!mNetworkInfo.isConnectedOrConnecting())
				return false;
			else
				if(openUrl())
					return true;
				else
					return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	
	public boolean openUrl()
	{
		String myString=null;
		try
		{
			//URL url=new URL("http://10.116.108.101:8080/DB_Server/");
			//URL url=new URL("http://10.64.32.64:8088/DB_Server/");
			URL url=new URL("http://192.168.0.107:8080/DB_Server/");
			URLConnection urlCon=url.openConnection();
			urlCon.setConnectTimeout(5000);
			InputStream is=urlCon.getInputStream();
			BufferedInputStream bis=new BufferedInputStream(is);
			ByteArrayBuffer baf=new ByteArrayBuffer(50);
			int current=0;
			while((current=bis.read())!=-1)
			{
				baf.append((byte)current);
			}
			
			myString=EncodingUtils.getString(baf.toByteArray(), "UTF-8");
			bis.close();
			is.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(myString.indexOf("JSP")>-1)
			return true;
		else
			return false;
	}
	
	@TargetApi(11)
	public void networkOnMainThread()
	{
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
	}
	
	
	public void dnQuery()
	{
		String dn=dn_no.getText().toString();
		String result=null;
		String []res=null;
		if(!isNetworkConnected(getActivity()))
		{
			new AlertDialog.Builder(getActivity())
			.setTitle("提示")
			.setMessage("網絡不可用，請檢查網絡連接！")
			.setPositiveButton("確定", null)
			.show();
		}
		else
		{
			if(validate(dn))
			{
				result=queryDN(dn);
				if(result.equals("Dn"))
				{
					new AlertDialog.Builder(getActivity())
					.setTitle("提示")
					.setMessage("該DN不存在！")
					.setPositiveButton("確定", null)
					.show();
				}
				else if(result.equals("Ship"))
				{
					new AlertDialog.Builder(getActivity())
					.setTitle("提示")
					.setMessage("該DN已經出貨！")
					.setPositiveButton("確定", null)
					.show();
				}
				else if(result.equals("Fail"))
				{
					new AlertDialog.Builder(getActivity())
					.setTitle("提示")
					.setMessage("未知原因無法查詢DN，請聯繫MIS！")
					.setPositiveButton("確定", null)
					.show();
				}
				else
				{
					res=result.split("\\|");
//					model_name.setText(res[0]);
//					ship_qty.setText(res[1]);
					ship_qty.setText("12312");
					model_name.setText("24324");
				}
			}
			else
			{
				new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage("DN號不能為空！")
				.setPositiveButton("確定", null)
				.show();
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.ship_fragment, container, false);

		emp_no = (TextView) v.findViewById(R.id.emp_no);

		networkOnMainThread();
		
		login = (Button) v.findViewById(R.id.login);
		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//Toast.makeText(getActivity(), "Start Login", Toast.LENGTH_SHORT).show();
				if(!isNetworkConnected(getActivity()))
				{
					new AlertDialog.Builder(getActivity())
					.setTitle("提示")
					.setMessage("網絡不可用，請檢查網絡連接！")
					.setPositiveButton("確定", null)
					.show();
				}
				else
				{
					String emp = emp_no.getText().toString().toUpperCase();
					if(validate(emp))
					{
						String stemp=login(emp);
						if(stemp.equals("OK"))
						{
							Toast.makeText(getActivity(),"登 陸 成 功！", Toast.LENGTH_SHORT).show();
							emp_no.setEnabled(false);
							dn_no.setEnabled(true);
							model_name.setEnabled(true);
							ship_qty.setEnabled(true);
							shipButton.setEnabled(true);
							dnButton.setEnabled(true);
							dnScanButton.setEnabled(true);
							scanButton.setEnabled(true);
						}
						else if(stemp.equals("Emp"))
						{
							new AlertDialog.Builder(getActivity())
							.setTitle("提示")
							.setMessage("員工不存在！")
							.setPositiveButton("確定", null)
							.show();
						}
						else if(stemp.equals("Privilege"))
						{
							new AlertDialog.Builder(getActivity())
							.setTitle("提示")
							.setMessage("該員工無權限！")
							.setPositiveButton("確定", null)
							.show();
						}
						else 
						{
							new AlertDialog.Builder(getActivity())
							.setTitle("提示")
							.setMessage("未知原因登陸失敗，請聯繫MIS！")
							.setPositiveButton("確定", null)
							.show();
						}		
					}
					else
					{
						new AlertDialog.Builder(getActivity())
						.setTitle("提示")
						.setMessage("權限不能為空！")
						.setPositiveButton("確定", null)
						.show();
					}
				}
			}
		});

		dn_no = (TextView) v.findViewById(R.id.dn_no);
		dn_no.setEnabled(false);
		
		unit_list=(TextView)v.findViewById(R.id.unit_list);
		unit_qty=(TextView)v.findViewById(R.id.unit_qty);
		
		scanButton=(Button)v.findViewById(R.id.scanningCode);
		scanButton.setEnabled(false);
		PackageManager pm=getActivity().getPackageManager();
		boolean hasCamera=pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)||
				pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)||
				Build.VERSION.SDK_INT<Build.VERSION_CODES.GINGERBREAD||
				Camera.getNumberOfCameras()>0;
		if(!hasCamera)
			scanButton.setEnabled(false);
		scanButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i=new Intent(getActivity(),CaptureActivity.class);
				startActivityForResult(i, 0);
			}
		});
		
		dnButton=(ImageButton)v.findViewById(R.id.dn_ok);
		dnButton.setEnabled(false);
		dnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dnQuery();
				
			}
		});
		
		dnScanButton=(ImageButton)v.findViewById(R.id.scan_dn);
		dnScanButton.setEnabled(false);
		dnScanButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i=new Intent(getActivity(),CaptureActivity.class);
				startActivityForResult(i,1);
			}
		});
		
		model_name = (TextView) v.findViewById(R.id.model_name);
		model_name.setEnabled(false);

		ship_qty = (TextView) v.findViewById(R.id.ship_qty);
		ship_qty.setEnabled(false);

		shipButton = (Button) v.findViewById(R.id.ship_btn);
		shipButton.setEnabled(false);
		shipButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(myAdapter.al.size()==0)
					new AlertDialog.Builder(getActivity())
						.setTitle("提示")
						.setMessage("請掃入要Shipping的包裝單位！")
						.setPositiveButton("確定", null)
						.show();
				else
				{
					int so_qty=Integer.parseInt(ship_qty.getText().toString());
					int to_qty=Integer.parseInt(total_qty.getText().toString());
					if(so_qty!=to_qty)
					{
						new AlertDialog.Builder(getActivity())
								.setTitle("提示")
								.setMessage("出貨數量與本次SO_QTY數量不一致，無法出貨！")
								.setPositiveButton("確定", null)
								.show();
					}
					else if(!isNetworkConnected(getActivity()))
					{
						new AlertDialog.Builder(getActivity())
							.setTitle("提示")
							.setMessage("網絡不可用，請檢查網絡連接！")
							.setPositiveButton("確定", null)
							.show();
					}
					else
					{
						String list="";
						String unit_list="";
						int size=myAdapter.al.size();
						for(int i=0;i<size-1;i++)
						{
							list=list+splitStr(myAdapter.al.get(i))+"x";
							unit_list=unit_list+splitStr(myAdapter.al.get(i))+",";
							
						}
						list=list+splitStr(myAdapter.al.get(size-1));
						unit_list=unit_list+splitStr(myAdapter.al.get(size-1));
						String emp=emp_no.getText().toString();
						String qty=ship_qty.getText().toString();
						String dn=dn_no.getText().toString();
						result=ship(emp,list,qty,dn);
					
						if(result.equals("OK"))
						{
							new AlertDialog.Builder(getActivity())
								.setTitle("提示")
								.setMessage("產品：  "+unit_list+"  出貨完成！")
								.setPositiveButton("確定", null)
								.show();
							dn_no.setText("");
							model_name.setText("");
							ship_qty.setText("");
							myAdapter.al.clear();
							myAdapter.notifyDataSetChanged();
							total_qty.setText("");
						}
						else
							new AlertDialog.Builder(getActivity())
								.setTitle("提示")
								.setMessage("無法出貨("+unit_list+")，請儘快聯繫MIS！")
								.setPositiveButton("確定", null)
								.show();
					}
				}
			}
		});

		sp = (Spinner) v.findViewById(R.id.shipping_by);
		list.add("Carton");
		list.add("Pallet");
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(adapter);
		sp.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				if(adapter.getItem(arg2)=="Pallet")
				{
					unit_list.setText("Pallet List");
					unit_qty.setText("Pallet Qty");
				}
				else
				{
					unit_list.setText("Carton List");
					unit_qty.setText("Carton Qty");
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		sp.setVisibility(View.VISIBLE);
		
		scanlist=(ListView)v.findViewById(R.id.listView_ship);
		myAdapter=new MyAdapter(getActivity());
		scanlist.setAdapter(myAdapter);
		scanlist.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				final int index=arg2;
				AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
				builder.setMessage("確認刪除？").setPositiveButton("確認", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						myAdapter.al.remove(index);
						myAdapter.notifyDataSetChanged();
						updateQty();
					}
				}).setNegativeButton("取消", null).create().show();
				return false;
			}
			
		});
		
		total_qty=(TextView)v.findViewById(R.id.scan_total_qty);
		total_qty.setText("0");
		
		return v;
	}

	
	private class MyAdapter extends BaseAdapter
	{
		ArrayList<String> al;
		private Context context;
		public MyAdapter(Context context)
		{
			super();
			this.setContext(context);
			al=new ArrayList<String>();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return al.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView==null)
				convertView=getActivity().getLayoutInflater().inflate(R.layout.scan_list, null);
			String s=al.get(position);
			if(s.lastIndexOf("|")!=-1)
			{
				String []res=s.split("\\|");
			
				unit_list=(TextView)convertView.findViewById(R.id.carton_list);
				unit_list.setText(res[0]);
			
				unit_qty=(TextView)convertView.findViewById(R.id.qty_list);
				unit_qty.setText(res[1]);
			}
			return convertView;
		}

		public void setContext(Context context) {
			this.context = context;
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode==Activity.RESULT_OK && requestCode==1)
		{
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			dn_no.setText(scanResult);
			dnQuery();	
		}
		if (resultCode == Activity.RESULT_OK && requestCode==0) {
			Bundle bundle = data.getExtras();
			String scanResult = bundle.getString("result");
			int flag=0;
			String model=model_name.getText().toString();
			result=queryQty(model,scanResult);
			if(result.equals("unit"))
				new AlertDialog.Builder(getActivity())
						.setTitle("提示")
						.setMessage("該Carton/Pallet不存在或已經出貨！")
						.setPositiveButton("確定", null)
						.show();
			else if(result.equals("None"))
				new AlertDialog.Builder(getActivity())
						.setTitle("提示")
						.setMessage("未知原因無法查詢QTY，請聯繫MIS！")
						.setPositiveButton("確定", null)
						.show();
			else
			{
				String []resp=result.split("\\|");
				if(unit_list.getText().equals("Pallet List"))
				{
					if(resp[0].startsWith("PE")&&resp[0].length()==11)
					{
						for(int i=0;i<myAdapter.al.size();i++)
						{
							if(resp[0].equals(myAdapter.al.get(i).substring(0, 11)))
								flag=1;
						}
						if(flag==1)
							new AlertDialog.Builder(getActivity())
								.setTitle("提示")
								.setMessage("該Shippng單位已經掃描過！")
								.setPositiveButton("確定", null)
								.show();
						else
						{
							myAdapter.al.add(result);
							myAdapter.notifyDataSetChanged();
						}	
					}
					else
					{
						new AlertDialog.Builder(getActivity())
								.setTitle("提示")
								.setMessage("請確認你所掃描的是Pallet單位！")
								.setPositiveButton("確定", null)
								.show();
					}			
				}
				else
				{
					if(resp[0].startsWith("CE")&&resp[0].length()==11)
					{
						for(int i=0;i<myAdapter.al.size();i++)
						{
							if(resp[0].equals(myAdapter.al.get(i).substring(0, 11)))
								flag=1;
						}
						if(flag==1)
							new AlertDialog.Builder(getActivity())
									.setTitle("提示")
									.setMessage("該Shippng單位已經掃描過！")
									.setPositiveButton("確定", null)
									.show();
						else
						{
							myAdapter.al.add(result);
							myAdapter.notifyDataSetChanged();
						}
					}
					else
					{
						new AlertDialog.Builder(getActivity())
								.setTitle("提示")
								.setMessage("請確認你所掃描的是Carton單位！")
								.setPositiveButton("確定", null)
								.show();
					}			
				}
			}
		}
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_list, menu);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{
			case R.id.change_user:
				init();
				return true;
			default:
				return super.onOptionsItemSelected(item);	
		}
		
	}
	
	public void init()
	{
		emp_no.setEnabled(true);
		emp_no.setText("");
		dnButton.setEnabled(false);
		dnScanButton.setEnabled(false);
		dn_no.setText("");
		dn_no.setEnabled(false);
		model_name.setText("");
		ship_qty.setText("");
		shipButton.setEnabled(false);
		myAdapter.al.removeAll(list);
		myAdapter.notifyDataSetChanged();
		scanButton.setEnabled(false);
		total_qty.setText("");	
	}
	
	public void updateQty()
	{
		int count=0;
		for(int i=0;i<myAdapter.al.size();i++)
		{
			String temp=myAdapter.al.get(i);
			String []stemp=temp.split("\\|");
			count+=Integer.parseInt(stemp[1]);
		}
		total_qty.setText(Integer.toString(count));	
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateQty();
	}
}
