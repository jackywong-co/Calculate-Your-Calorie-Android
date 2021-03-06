package com.example.calculateyourcalorie;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;


import com.example.calculateyourcalorie.RoomDataBase.Item;
import com.example.calculateyourcalorie.RoomDataBase.ItemDao;
import com.example.calculateyourcalorie.RoomDataBase.ItemDataBase;

import java.util.List;

public class ItemRepository {
    private ItemDao itemDao;
    private LiveData<List<Item>> allItems;
    private LiveData<Integer> totalCalories;
    public ItemRepository(Application application) {
        ItemDataBase dataBase = ItemDataBase.getInstance(application);
        itemDao = dataBase.itemDao();
        allItems = itemDao.getAllItems();
        totalCalories = itemDao.getTotalCalories();
    }

    public void insert(Item item) {
        new InsertItemAsyncTask(itemDao).execute(item);
    }

    public void update(Item item) {
        new UpdateItemAsyncTask(itemDao).execute(item);
    }

    public void delete(Item item) {
        new DeleteItemAsyncTask(itemDao).execute(item);
    }

    public void deleteAllItems() {
        new DeleteAllItemAsyncTask(itemDao).execute();
    }

    public LiveData<List<Item>> getAllItems() {
        return allItems;
    }
    public LiveData<Integer> getTotalCalories() {
        return totalCalories;
    }

    private static class InsertItemAsyncTask extends AsyncTask<Item, Void, Void> {

        private ItemDao itemDao;

        private InsertItemAsyncTask(ItemDao itemUao) {
            this.itemDao = itemUao;
        }

        @Override
        protected Void doInBackground(Item... items) {
            itemDao.insert(items[0]);
            return null;
        }
    }

    private static class UpdateItemAsyncTask extends AsyncTask<Item, Void, Void> {

        private ItemDao itemDao;

        private UpdateItemAsyncTask(ItemDao itemUao) {
            this.itemDao = itemUao;
        }

        @Override
        protected Void doInBackground(Item... items) {
            itemDao.update(items[0]);
            return null;
        }
    }

    private static class DeleteItemAsyncTask extends AsyncTask<Item, Void, Void> {

        private ItemDao itemDao;

        private DeleteItemAsyncTask(ItemDao itemDao) {
            this.itemDao = itemDao;
        }

        @Override
        protected Void doInBackground(Item... items) {
            itemDao.delete(items[0]);
            return null;
        }
    }

    private static class DeleteAllItemAsyncTask extends AsyncTask<Void, Void, Void> {

        private ItemDao itemDao;

        private DeleteAllItemAsyncTask(ItemDao itemDao) {
            this.itemDao = itemDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            itemDao.deleteAllItem();
            return null;
        }
    }
}
