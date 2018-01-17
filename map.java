public class MidFragment extends BaseFragment {
    private static final String TITLE = "中间";
    private MapView mMapView;//地图
    private Button mLocateBT, mResetBT;//定位、重置按钮
    private RadioGroup mTypeRG;
    private double latitude = 30.343112;//当前点纬度
    private double longitude = 120.11522;//当前点经度
    private ImageView mSouthIV;//指南针
    private SensorManager mSensorManager;//方向传感器
    private GraphicsLayer mMyLayer;//当前位置的图层
    private int mMyUID;//当前位置的图标ID
    private PictureMarkerSymbol mLocationSymbol;//当前位置的图标
    private Callout mCallout;//地图弹窗
    private ArcGISDynamicMapServiceLayer mServiceLayer;
    private String url = "http://192.168.1.235:6080/arcgis/rest/services/SanKeFolder/SanKe_0426/MapServer";
    //    private String url = "http://huangyi2016.iask.in:23527/arcgis/rest/services/SanKeFolder/SanKe_0426/MapServer";
    //    private String url = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
    private Point mClickPoint;//地图点击点
    private IdentifyParameters params = null;//搜索条件
    private boolean isNavigation = false;//是否是导航模式
    private boolean isRomation = false;//是否正在旋转
    private float mMapDegree;//地图旋转角度
    private float mSensorDegree;//传感器角度
    private int WKID_IN = 4326;//输入坐标系
    private int WKID_OUT = 4490;//输出坐标系
    private String stringBuffer = "经纬度：\n";
    private TextView gpsTextView;

    private EntScenicSpot mScenicSpot;//景点
    private LinearLayout mBottomLL;//底部景点信息布局
    private TextView mBottomNameTV, mBottomDetailTV;//景点名称，景点详情
    private ImageView mBottomIV;//景点图片
    private boolean isBottomShow;//是否显示底部景点信息

    public static MidFragment getInstance() {
        return new MidFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == mView) {
            mView = inflater.inflate(R.layout.fragment_mid, null);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initView() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);  // 获得传感器管理器
        mCommonHeaderBar.initHeaderStyle(CommonHeaderBar.HeaderStyle.MIDTV);
        mCommonHeaderBar.setMidText(TITLE);
        mMapView = (MapView) findViewById(R.id.view_map);
        mResetBT = (Button) findViewById(R.id.bt_reset);
        mLocateBT = (Button) findViewById(R.id.bt_locate);
        mSouthIV = (ImageView) findViewById(R.id.iv_south);
        mTypeRG = (RadioGroup) findViewById(R.id.rg_type);
        gpsTextView = (TextView) findViewById(R.id.tv_gps);
        mBottomLL = (LinearLayout) findViewById(R.id.ll_bottom);
        mBottomNameTV = (TextView) findViewById(R.id.tv_name);
        mBottomDetailTV = (TextView) findViewById(R.id.tv_detail);
        mBottomIV = (ImageView) findViewById(R.id.iv_info);
        mCallout = mMapView.getCallout();//通过MapView获取Callout实例对象
        mCallout.setStyle(R.xml.mime_callout_style);//为Callout设置样式文件
        mLocationSymbol = new PictureMarkerSymbol(getActivity().getResources().getDrawable(R.drawable.ic_map_arrow));//设置当前点的图标样式
    }

    @Override
    protected void initData() {
        ArcGISRuntime.setClientId("9yNxBahuPiGPbsdi");//去水印

        //获取到经纬度就设置，没有就设置默认的
        latitude = SharePreferenceUtil.getInstance().getString("LATITUDE") == null ? 30 : Double.valueOf(SharePreferenceUtil.getInstance().getString("LATITUDE"));
        longitude = SharePreferenceUtil.getInstance().getString("LONGTIUDE") == null ? 120 : Double.valueOf(SharePreferenceUtil.getInstance().getString("LONGTIUDE"));
        Log.v("--->获取", longitude + "/" + latitude);
        Toast.makeText(getActivity(), longitude + "/" + latitude, Toast.LENGTH_LONG).show();


        mServiceLayer = new ArcGISDynamicMapServiceLayer(url);//在线地图
        mMapView.addLayer(mServiceLayer);//添加图层
        mMapView.setAllowRotationByPinch(true); //是否允许使用Pinch方式旋转地图
        mMapView.setMapBackground(getResources().getColor(R.color.white), getResources().getColor(R.color.white), 0, 0);//设置背景


        //设置当前位置为中心点
        Point wgsPoint = new Point(longitude, latitude);
        Point mapPoint = (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(WKID_IN), SpatialReference.create(WKID_OUT));
        mMapView.centerAt(mapPoint, true);

        Graphic graphic = new Graphic(mapPoint, mLocationSymbol);
        mMyLayer = new GraphicsLayer();
        mMyUID = mMyLayer.addGraphic(graphic);
        mMapView.addLayer(mMyLayer);//添加覆盖物图层

        // 限定当前显示区域
//        Unit mapUnit = SpatialReference.create(3857).getUnit();//mMapView.getSpatialReference().getUnit();
//        double zoomWidth = Unit.convertUnits(10, Unit.create(LinearUnit.Code.MILE_US), mapUnit);
//        Envelope zoomExtent = new Envelope(mapPoint, zoomWidth, zoomWidth);
//        mMapView.setExtent(zoomExtent);
    }

    @Override
    protected void initListener() {
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {//地图的状态监听
            @Override
            public void onStatusChanged(Object o, STATUS status) {
                if (status.equals(STATUS.INITIALIZED)) { //初始化完成才显示，防止黑屏
                    mMapView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMapView.setVisibility(View.VISIBLE);
                        }
                    }, 100);

                }
            }
        });

        mResetBT.setOnClickListener(new View.OnClickListener() {//重置
            @Override
            public void onClick(View v) {
                isNavigation = false;//关闭导航模式
                mMapView.setRotationAngle(0); //初始化时地图角度，参数为正时按逆时针方向旋转
                Point wgsPoint = new Point(longitude, latitude);
                Point mapPoint = (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(WKID_IN), SpatialReference.create(WKID_OUT));
                mMapView.centerAt(mapPoint, true);
                mSouthIV.setRotation(0);

//                gpsTextView.setVisibility(View.INVISIBLE);
            }
        });

        mLocateBT.setOnClickListener(new View.OnClickListener() {//定位
            @Override
            public void onClick(View v) {
                isNavigation = true;//启动导航模式

                //当前位置图标的更新
                Point wgsPoint = new Point(longitude, latitude);
                Point mapPoint = (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(WKID_IN), SpatialReference.create(WKID_OUT));
                mLocationSymbol.setAngle(0);
                mMapView.centerAt(mapPoint, true);
                Graphic graphic = new Graphic(mapPoint, mLocationSymbol);
                mMyLayer.updateGraphic(mMyUID, graphic);

//                gpsTextView.setVisibility(View.VISIBLE);
//                gpsTextView.setText(stringBuffer);
            }
        });

        mMapView.setOnPinchListener(new OnPinchListener() {//地图旋转监听
            @Override
            public void prePointersMove(float v, float v1, float v2, float v3, double v4) {
            }

            @Override
            public void postPointersMove(float v, float v1, float v2, float v3, double v4) {
                mSouthIV.post(new Runnable() {
                    @Override
                    public void run() {
                        mMapDegree = (float) mMapView.getRotationAngle();
                        mSouthIV.setRotation(-mMapDegree);
                    }
                });
            }

            @Override
            public void prePointersDown(float v, float v1, float v2, float v3, double v4) {
            }

            @Override
            public void postPointersDown(float v, float v1, float v2, float v3, double v4) {
                isRomation = true;
            }

            @Override
            public void prePointersUp(float v, float v1, float v2, float v3, double v4) {
            }

            @Override
            public void postPointersUp(float v, float v1, float v2, float v3, double v4) {
                isRomation = false;
            }
        });


        //高德定位监听
        ToolGDLocate.initNewInstance(getActivity());
        ToolGDLocate.startLocate(new ToolGDLocate.GDListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (Math.abs(amapLocation.getLatitude() - latitude) > 0.00005 || Math.abs(amapLocation.getLongitude() - longitude) > 0.00005) {
                    stringBuffer += (longitude + " - " + latitude + "\n");
                }
                longitude = amapLocation.getLongitude();
                latitude = amapLocation.getLatitude();
                Point wgsPoint = new Point(longitude, latitude);
                Point mapPoint = (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(WKID_IN), SpatialReference.create(WKID_OUT));
                Graphic graphic = new Graphic(mapPoint, mLocationSymbol);
                mMyLayer.updateGraphic(mMyUID, graphic);
                Log.v("--->更新", longitude + "/" + latitude);
            }
        });

        mMapView.setOnSingleTapListener(//覆盖物监听
                new OnSingleTapListener() {
                    @Override
                    public void onSingleTap(float v, float v1) {

                        if (!mMapView.isLoaded())
                            return;

                        mCallout.hide();
                        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.view_callout_locate, null);//弹出窗口布局文件对象

                        int[] graphicIDs = mMyLayer.getGraphicIDs(v, v1, 25);
                        if (graphicIDs != null && graphicIDs.length > 0) {
                            Graphic gr = mMyLayer.getGraphic(graphicIDs[0]);
                            Point location = (Point) gr.getGeometry();
                            mCallout.setOffset(0, -15);//设置偏移量
                            mCallout.show(location, mView);//设置弹出窗显示的内容
                        }


                        mClickPoint = mMapView.toMapPoint(v, v1);

                        //关键词查询设置
                        String targetLayer = url.concat("/0");
//                        String[] queryArray = {targetLayer, "Name = '厕所'"};
                        String[] queryArray = {targetLayer, "Message like '%厕所%'"};
                        AsyncQueryTask ayncQuery = new AsyncQueryTask();
                        ayncQuery.execute(queryArray);


                        //触碰点查询设置
                        params = new IdentifyParameters();
                        params.setTolerance(20);
                        params.setDPI(98);
//                params.setLayers(new int[]{4});
                        params.setLayerMode(IdentifyParameters.ALL_LAYERS);
                        params.setGeometry(mClickPoint);
                        params.setSpatialReference(mMapView.getSpatialReference());
                        params.setMapHeight(mMapView.getHeight());
                        params.setMapWidth(mMapView.getWidth());
                        params.setReturnGeometry(true);

                        // add the area of extent to identify parameters
                        Envelope env = new Envelope();
                        mMapView.getExtent().queryEnvelope(env);
                        params.setMapExtent(env);

                        // execute the identify task off UI thread
                        MyIdentifyTask mTask = new MyIdentifyTask(mClickPoint);
                        mTask.execute(params);

                    }
                }

        );


        mTypeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_map_all:
                        mServiceLayer.setVisible(true);
                        break;
                    case R.id.rb_map_point:
                        mServiceLayer.setVisible(false);
                        break;
                    case R.id.rb_map_wc:
                        ArcGISLayerInfo[] infos = mServiceLayer.getLayers();
                        infos[0].setVisible(true);
                        mServiceLayer.refresh();
                        break;
                    case R.id.rb_map_store:
                        ArcGISLayerInfo[] infoss = mServiceLayer.getLayers();
                        infoss[0].setVisible(false);
                        mServiceLayer.refresh();
                        break;
                    case R.id.rb_map_door:
                        break;
                }
            }
        });

        mBottomLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "跳转更多", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //关键字查询
    private class AsyncQueryTask extends AsyncTask {

        @Override
        protected FeatureResult doInBackground(String... queryArray) {

            if (queryArray == null || queryArray.length <= 1)
                return null;

            String url = queryArray[0];
            QueryParameters qParameters = new QueryParameters();
            String whereClause = queryArray[1];
            SpatialReference sr = SpatialReference.create(102100);
//            qParameters.setGeometry(mMapView.getExtent());
//            qParameters.setGeometry(mClickPoint);
            qParameters.setOutSpatialReference(sr);
            qParameters.setReturnGeometry(true);
            qParameters.setWhere(whereClause);

            QueryTask qTask = new QueryTask(url);

            try {
                return qTask.execute(qParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(FeatureResult results) {

            if (results != null) {
//                Toast.makeText(getActivity(), "您找到了一个厕所", Toast.LENGTH_SHORT).show();
                for (Object element : results) {
                    if (element instanceof Feature) {
                        Feature feature = (Feature) element;
                        Graphic graphic = new Graphic(feature.getGeometry(), feature.getSymbol(), feature.getAttributes());
//                        graphicsLayer.addGraphic(graphic);
                        //结果添加事件
                        mCallout.hide();
                        View mView = LayoutInflater.from(getActivity()).inflate(R.layout.view_callout_locate, null);//弹出窗口布局文件对象
                        Point location = (Point) graphic.getGeometry();
                        mCallout.setOffset(0, -15);//设置偏移量
                        mCallout.show(location, mView);//设置弹出窗显示的内容
                    }
                }
            }

        }

    }


    //触碰点查询
    private class MyIdentifyTask extends AsyncTask {

        IdentifyTask task = new IdentifyTask(url);

        IdentifyResult[] M_Result;

        Point mAnchor;

        MyIdentifyTask(Point anchorPoint) {
            mAnchor = anchorPoint;
        }

        protected IdentifyResult[] doInBackground(IdentifyParameters... params) {

            if (params != null && params.length > 0) {
                IdentifyParameters mParams = params[0];
                try {
                    M_Result = task.execute(mParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return M_Result;
        }

        @Override
        protected void onPostExecute(IdentifyResult[] results) {

            if (results != null && results.length > 0) {
                Log.v("-->", results[0].getDisplayFieldName());
                mCallout.hide();

                if (results[0].getAttributes().get("Name") != null && results[0].getAttributes().get("Name").equals("洗手间")) {
                    View mView = LayoutInflater.from(getActivity()).inflate(R.layout.view_callout_locate, null);//弹出窗口布局文件对象
                    Geometry geometry = results[0].getGeometry();
                    mCallout.setOffset(0, -15);//设置偏移量
                    mCallout.show((Point) geometry, mView);//设置弹出窗显示的内容
                } else if (results[0].getAttributes().get("SHAPE") != null && results[0].getAttributes().get("SHAPE").equals("点")) {
                    if (results[0].getAttributes().get("Message") != null)
                        getScenicSpot(results[0].getAttributes().get("Message").toString());
                } else if (results[0].getAttributes().get("SHAPE") == null && isBottomShow) {
                    showBottom(false);
                } else if (results[0].getAttributes().get("SHAPE") != null && !results[0].getAttributes().get("SHAPE").equals("点") && isBottomShow) {
                    showBottom(false);
                }
            } else if (isBottomShow) {
                showBottom(false);
            }

//            ArrayList resultList = new ArrayList();
//
//            IdentifyResult result_1;
//
//            for (int index = 0; index < results.length; index++) {
//
//                result_1 = results[index];
//                String displayFieldName = result_1.getDisplayFieldName();
//                Map attr = result_1.getAttributes();
//                for (String key : attr.keySet()) {
//                    if (key.equalsIgnoreCase(displayFieldName)) {
//                        resultList.add(result_1);
//                    }
//                }
//            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.unpause();
        mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(sensorEventListener);
    }

    /**
     * Tip：
     * ArcGISTiledMapServiceLayer类，这个类可以用来加载基础图层，但是默认是访问URL获取的，只要重写它的getTile()方法，
     * 在获取瓦片的时候先判断本地是否存在，如果本地不存在则使用super.getTile()方法获得URL中的相应瓦片，然后保存到本地。
     */
    private SensorEventListener sensorEventListener = new SensorEventListener() {//方向传感器监听

        @Override
        public void onSensorChanged(final SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                // 获取当前传感器获取到的角度
                if (isNavigation && isRomation) {//记录手动旋转时传感器的角度
                    mSensorDegree = event.values[0];
                } else if (isNavigation && !isRomation) {//旋转时调整地图角度
                    float degree = mMapDegree + event.values[0] - mSensorDegree;
//                    Point centerPoint = (Point) GeometryEngine.project(new Point(longitude, latitude), SpatialReference.create(WKID_IN), SpatialReference.create(WKID_OUT));
//                    mMapView.centerAt(centerPoint, true);//设置中心点
                    mMapView.setRotationAngle(degree);
                    mSouthIV.setRotation((float) -mMapView.getRotationAngle());
                } else if (!isNavigation) {
                    float degree = event.values[0];
                    //当前位置图标的更新
                    Point wgsPoint = new Point(longitude, latitude);
                    Point mapPoint = (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(WKID_IN), SpatialReference.create(WKID_OUT));
                    mLocationSymbol.setAngle(degree);
                    Graphic graphic = new Graphic(mapPoint, mLocationSymbol);
                    mMyLayer.updateGraphic(mMyUID, graphic);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    //底部信息布局的显示/隐藏
    private void showBottom(boolean toShow) {
        if (toShow) {
            //底部信息布局的显示
            mBottomLL.setVisibility(View.VISIBLE);
            ObjectAnimator animation = ObjectAnimator.ofFloat(mBottomLL, "translationY", mBottomLL.getLayoutParams().height, 0);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(300);
            animation.start();
            isBottomShow = true;
        } else {
            //底部信息布局的隐藏
            ObjectAnimator animation = ObjectAnimator.ofFloat(mBottomLL, "translationY", 0, mBottomLL.getLayoutParams().height);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(300);
            animation.start();
            isBottomShow = false;
        }
    }

    /**
     * 获取景点
     */
    private void getScenicSpot(final String id) {
        putAsyncTask(new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected DataExchange doInBackground(Void... params) {
                String strXML = HttpUtil.request(mXMLGenerator.getScenicSpotInfo(id));
                DataExchange dataExg = mXMLResolver.getScenicSpotInfo(strXML);
                return dataExg;
            }

            @Override
            protected void onPostExecute(DataExchange result) {
                super.onPostExecute(result);
                dismissLoadingDialog();
                if (result.isSuccess()) {
                    List spots = (List) result.getBackData();
                    if (spots != null) {
                        mScenicSpot = spots.get(0);
                        mBottomNameTV.setText(mScenicSpot.getName());
                        mBottomDetailTV.setText(mScenicSpot.getDesc());
                        Glide.with(getActivity()).load(mScenicSpot.getPicpath()).into(mBottomIV);
                        if (!isBottomShow)
                            showBottom(true);
                    }
                } else {
                    if (!result.getErrorInfo().isEmpty())
                        showCustomToast(result.getErrorInfo());
                    else
                        showCustomToast(R.string.individuality_dialog);
                }
            }
        });
    }
}