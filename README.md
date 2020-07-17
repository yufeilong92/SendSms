# SendSms
应公司要求做一个群发短信功能
去重，合并
```
   private fun Goheavy(filter: List<String>) {
        //去重
        val set = TreeSet(filter)
        val result = mutableListOf<String>()
        result.addAll(set)
        //并集
        mArray?.removeAll(result)
        addData(result)
        mAdapter?.notifyDataSetChanged()
        Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
    }

```

导入通讯录
```
   val list= mutableListOf<String>()
        contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,null,null,null)?.apply {
            while (moveToNext()){
                val phone=getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                list.add(phone)
            }
            //过滤为空的值
            val filter = list.filter { ""!=it&&isPhoneNum(it.trim().replace(" ","")) }
            Goheavy(filter)
        }

```
跳转到文件管理器
```
       val intent1 = Intent(Intent.ACTION_GET_CONTENT)
                intent1.setType("*/*")
                intent1.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent1, REQEUESTCODE)
                
                
       override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        var path: String? = null
        if (requestCode == REQEUESTCODE) {
            val rui = data.data
            if ("file".equals(rui!!.scheme)) {
                path = rui.path
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                path = FileUtil.getPath(this, rui)
            } else
                path = FileUtil.getRealPathFromURI(this, rui)
            if (TextUtils.isEmpty(path)) {
                return
            } else {
                val contains = path!!.contains(".txt")
                if (!contains) {
                    Toast.makeText(this, "请选择txt 文件", Toast.LENGTH_SHORT).show();
                    return
                }
                bindViewData(path)
            }
        }


    }
        
     private fun bindViewData(path: String?) {
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "获取路径失败", Toast.LENGTH_SHORT).show();
            return
        }
        val file = File(path)
        if (!file.exists()) return
        val it = FileInputStream(file)
        if (it != null) {
            val line = mReadTxtFile(path!!)
            val split = line?.split(";") as MutableList<String>
            //过滤为空的值
            val filter = split.filter { "" != it&&isPhoneNum(it.trim().replace(" ","")) }
            Goheavy(filter)
        }

    }
    
        /**
     * 判断是否为手机号码
     *
     * @return
     */
    fun isPhoneNum(phone: String): Boolean {
        val pattern = Pattern.compile("1[0-9]{10}")
        val matcher = pattern.matcher(phone)
        return matcher.matches()
    }
    
    // 长短信发送方式1
        val sendIntent = Intent("SENT_SMS_ACTION");
        val sendPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sendIntent, 0);
        val deliverIntent = Intent("DELIVERED_SMS_ACTION");
        val deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        val smsManager = SmsManager.getDefault();
        var divideContents = smsManager.divideMessage("$com")
        for (text in divideContents) {
            smsManager.sendTextMessage(sb.toString(), null, text, sendPI, deliverPI);
        }
        Handler().postDelayed({
            if (mShowProgressDialog != null && mShowProgressDialog!!.isShowing) {
                mShowProgressDialog?.dismiss()
            }
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        }, 2000)
```
```
public static String getRealPathFromURI(Activity a, Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = a.getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     *  * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     *  
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {


        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;


// DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
//                final String type = split[0];
//                if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
//                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {


                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));


                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];


                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }


                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};


                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
// MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
// File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     *  * Get the value of the data column for this Uri. This is useful for
     *  * MediaStore Uris, and other file-based ContentProviders.
     *  *
     *  * @param context    The context.
     *  * @param uri     The Uri to query.
     *  * @param selection  (Optional) Filter used in the query.
     *  * @param selectionArgs (Optional) Selection arguments used in the query.
     *  * @return The value of the _data column, which is typically a file path.
     *  
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {


        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};


        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     *  * @param uri The Uri to check.
     *  * @return Whether the Uri authority is ExternalStorageProvider.
     *  
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    /**
     *  * @param uri The Uri to check.
     *  * @return Whether the Uri authority is DownloadsProvider.
     *  
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    /**
     *  * @param uri The Uri to check.
     *  * @return Whether the Uri authority is MediaProvider.
     *  
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String mReadTxtFile(String strFilePath) {
        String path = String.valueOf(strFilePath);
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    String line=null;
                    BufferedReader buffreader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line+";";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }
```
