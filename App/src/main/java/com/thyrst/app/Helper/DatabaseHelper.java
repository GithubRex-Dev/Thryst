package com.thyrst.app.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.thyrst.app.Object.ShoppingList;
import com.thyrst.app.Object.Recipe;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static String TAG = "DatabaseHelper";// 用于记录日志
	private static final int DATABASE_VERSION = 1;//数据库版本
	private static final String DATABASE_NAME = "dataBaseThyrst.db";//数据库名称
	private static String DATABASE_PATH = "";//设备中数据库文件的存储路径
	private SQLiteDatabase mDataBase; // 声明一个SQLite数据库对象
	private final Context mContext;

	//表名
	private static final String FAV_TABLE = "FavRecipeTable";
	private static final String LIST_TABLE = "ShoppingListTable";
	//键名
	private static final String RECIPE_ID = "recipeID";
	private static final String LIST_CODE = "listIndex";

	public DatabaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		if(android.os.Build.VERSION.SDK_INT >= 17){
			DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
		}
		else{
			DATABASE_PATH  = "/data/data/" + context.getPackageName() + "/databases/";
		}

		this.mContext = context;
		this.DATABASE_PATH = this.mContext.getDatabasePath(DATABASE_NAME).getAbsolutePath();
		Log.e("Path 1", DATABASE_PATH);
	}

	public void createDataBase() throws IOException{
		//若数据库文件不存在，则从assets文件夹中拷贝到设备中
		boolean dataBaseExist = checkDataBase();
		if(!dataBaseExist){
			this.getWritableDatabase();
			this.close();
			try{
				//拷贝数据库文件
				copyDataBase();
				Log.e(TAG, "createDatabase database created");
			}catch (IOException mIOException){
				throw new Error("ErrorCopyingDataBase");
			}
		}
	}

	//检查待创建的数据库是否在设备本地存在: /data/data/your package/databases/DATABASE_NAME
	private boolean checkDataBase(){
		File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
		Log.v("dbFile", dbFile + "   "+ dbFile.exists());
		return dbFile.exists();
	}

	//将工程assets文件夹中的数据库文件复制到设备本地中
	private void copyDataBase() throws IOException{
		try{
			InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
			String outFileName = DATABASE_PATH + DATABASE_NAME;
			OutputStream mOutput = new FileOutputStream(outFileName);
			byte[] mBuffer = new byte[1024];
			int mLength;
			while ((mLength = mInput.read(mBuffer))>0) {
				mOutput.write(mBuffer, 0, mLength);
			}
			mOutput.flush();
			mOutput.close();
			mInput.close();
		}catch(IOException e){
			throw e;
		}
	}

	//开启本地数据库
	public boolean openDataBase() throws SQLException {
		String mPath = DATABASE_PATH + DATABASE_NAME;
		Log.v("mPath", mPath);
		mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
		return mDataBase != null;
	}

	// 建立一个本地数据库连接
	public void connectDataBase() throws SQLException,IOException {
		try {
			createDataBase();
			openDataBase();
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}

	//更新收藏列表
	public  void updateFavList(Recipe mRecipe,boolean isFav) throws Exception {
		if(mDataBase.isOpen()&&!mDataBase.isReadOnly()&&!mDataBase.inTransaction()) {
			try {
				mDataBase.beginTransaction();
				if (isFav){
					// 当前状态为确认收藏，添加该记录
					ContentValues row = new ContentValues();
					row.put("recipeID", mRecipe.getRecipeID());
					row.put("recipeName", mRecipe.getRecipeName());
					row.put("recipeRank", mRecipe.getRecipeRank());
					row.put("recipeBrief", mRecipe.getRecipeBrief());
					row.put("recipeCover", mRecipe.getRecipeCover());
					row.put("recipeVideo", mRecipe.getRecipeVideo());
					row.put("recipeDirection", mRecipe.getRecipeDirection());
					row.put("recipeIngredients", mRecipe.getRecipeIngredients());
					mDataBase.insert(FAV_TABLE, null, row);
				}
				else{
					// 当前状态为取消收藏，删除该记录
					mDataBase.delete(FAV_TABLE, RECIPE_ID + " = '" + mRecipe.getRecipeID()+"'", null);
				}
				mDataBase.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("DatabaseHandler", "Error msg: " + e.getMessage());
			} finally {
				mDataBase.endTransaction();
			}
		}
	}

	//获取收藏列表
	public synchronized ArrayList<Recipe> getFavList(){
		String selectQuery = "";
		selectQuery = "SELECT * FROM " + FAV_TABLE ;
		Cursor cursor = mDataBase.rawQuery(selectQuery, null);
		ArrayList<Recipe> FavList = new ArrayList<Recipe>();
		if (cursor.moveToFirst()) {
			do {
				Recipe mRecipe = new Recipe();
				mRecipe.setRecipeID(cursor.getString(0));
				mRecipe.setRecipeName(cursor.getString(1));
				mRecipe.setRecipeRank(Integer.parseInt(cursor.getString(2)));
				mRecipe.setRecipeBrief(cursor.getString(3));
				mRecipe.setRecipeCover(cursor.getString(4));
				mRecipe.setRecipeVideo(cursor.getString(5));
				mRecipe.setRecipeDirection(cursor.getString(6));
				mRecipe.setRecipeIngredients(cursor.getString(7));
				FavList.add(mRecipe);
			} while (cursor.moveToNext());
		}
		return FavList;
	}

	//获取购物清单列表
	public synchronized ArrayList<ShoppingList> getShoppingList(){
		String selectQuery = "";
		selectQuery = "SELECT * FROM " + LIST_TABLE +" ORDER BY "+LIST_CODE+" DESC";
		Cursor cursor = mDataBase.rawQuery(selectQuery, null);
		ArrayList<ShoppingList> spLists = new ArrayList<ShoppingList>();
		if (cursor.moveToFirst()) {
			do {
				ShoppingList mShoppingList = new ShoppingList();
				mShoppingList.setSpListID(cursor.getString(0));
				mShoppingList.setSpListName(cursor.getString(1));
				mShoppingList.setSpListItems(cursor.getString(2));
				spLists.add(mShoppingList);
			} while (cursor.moveToNext());
		}
		return spLists;
	}

	//新增购物清单
	public  void addShoppingList(String mIndex,String listName,String listItems) throws Exception {
		if(mDataBase.isOpen()&&!mDataBase.isReadOnly()&&!mDataBase.inTransaction()) {
			try {
				//添加该记录
				mDataBase.beginTransaction();
				ContentValues row = new ContentValues();
				row.put("listIndex", mIndex);
				row.put("listName", listName);
				row.put("listItems", listItems);
				mDataBase.insert(LIST_TABLE, null, row);
				mDataBase.setTransactionSuccessful();
			} catch (SQLException e) {
				e.printStackTrace();
				Log.d("DatabaseHandler", "Error msg: " + e.getMessage());
			} finally {
				mDataBase.endTransaction();
			}
		}
	}

	// 取消添加购物清单
	public  void UndoAddingList(String Index) throws Exception {
		if(mDataBase.isOpen()&&!mDataBase.isReadOnly()&&!mDataBase.inTransaction()) {
			try {
				//删除该记录
				mDataBase.beginTransaction();
				mDataBase.delete(LIST_TABLE, "listIndex = '" + Index+"'", null);
				mDataBase.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("DatabaseHandler", "Error msg: " + e.getMessage());
			} finally {
				mDataBase.endTransaction();
			}
		}
	}

	//更新购物清单
	public  void updateShoppingList(String listIndex, String listItems) throws Exception {
		if(mDataBase.isOpen()&&!mDataBase.isReadOnly()&&!mDataBase.inTransaction()) {
			try {
				//更新该记录
				mDataBase.beginTransaction();
				ContentValues row = new ContentValues();
				row.put("listIndex", listIndex);
				row.put("listItems", listItems);
				String[] args = {String.valueOf(listIndex)};
				mDataBase.update(LIST_TABLE, row, "listIndex = ?",args);
				mDataBase.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("DatabaseHandler", "Error msg: " + e.getMessage());
			} finally {
				mDataBase.endTransaction();
			}
		}
	}

	// 删除购物清单
	public  void removeShoppingList(ShoppingList mShoppingList) throws Exception {
		if(mDataBase.isOpen()&&!mDataBase.isReadOnly()&&!mDataBase.inTransaction()) {
			try {
				//删除该记录
				mDataBase.beginTransaction();
				mDataBase.delete(LIST_TABLE, LIST_CODE + " = '" + mShoppingList.getSpListID()+"'", null);
				mDataBase.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("DatabaseHandler", "Error msg: " + e.getMessage());
			} finally {
				mDataBase.endTransaction();
			}
		}
	}

	// 写收藏状态于SharedPreferences对象中
	public static void saveRecipeFavouriteState(Context mContext, String key, boolean isFavourite) {
		SharedPreferences aSharedPreferenes = mContext.getSharedPreferences(
				"Favourite", Context.MODE_PRIVATE);
		SharedPreferences.Editor aSharedPreferenesEdit = aSharedPreferenes
				.edit();
		aSharedPreferenesEdit.putBoolean("RecipeState"+key, isFavourite);
		aSharedPreferenesEdit.commit();
	}

	// 从SharedPreferences对象中读收藏状态
	public static boolean readRecipeFavouriteState(Context mContext, String key) {
		SharedPreferences aSharedPreferenes = mContext.getSharedPreferences(
				"Favourite", Context.MODE_PRIVATE);
		return aSharedPreferenes.getBoolean("RecipeState"+key, false);
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		// No need to write the create table query.
		// As we are using pre-built database.
		// Which is ReadOnly.
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion>oldVersion)
			try {
				copyDataBase();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public synchronized void close() {
		if(mDataBase != null)
			mDataBase.close();
		super.close();
	}
}