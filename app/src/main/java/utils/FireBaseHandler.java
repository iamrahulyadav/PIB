package utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by bunny on 04/10/17.
 */

public class FireBaseHandler {
    private FirebaseDatabase mFirebaseDatabase;

    public FireBaseHandler() {

        mFirebaseDatabase = FirebaseDatabase.getInstance();

    }


    public void uploadNews(final News news, final OnNewsListener onNewsListener) {


        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("news/");

        news.setNewsID(mDatabaseRef.push().getKey());

        news.setPushNotification(true);
        news.setDescription("Press Information Bureau");

        DatabaseReference mDatabaseRef1 = mFirebaseDatabase.getReference().child("news/" + news.getNewsID());


        mDatabaseRef1.setValue(news).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    onNewsListener.onNewsUpload(true);
                } else {
                    onNewsListener.onNewsUpload(false);
                }

            }
        });


    }

    public void downloadNewsList(int limit, final OnNewsListener onNewsListener) {


        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("news/");

        Query myref2 = mDatabaseRef.limitToLast(limit);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<News> newsArrayList = new ArrayList<News>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    News news = snapshot.getValue(News.class);
                    if (news != null) {

                        news.setNewsID(snapshot.getKey());

                    }
                    newsArrayList.add(news);
                }

                Collections.reverse(newsArrayList);

                onNewsListener.onNewsListDownload(newsArrayList, true);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });


    }

    public void uploadInitiatives(final News news, final OnNewsListener onNewsListener) {


       /* DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("Initiatives/");

        news.setNewsID(mDatabaseRef.push().getKey());

        news.setPushNotification(false);

        DatabaseReference mDatabaseRef1 = mFirebaseDatabase.getReference().child("Initiatives/" + news.getNewsID());

        news.setNewsID("Initiatives");

        mDatabaseRef1.setValue(news).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    onNewsListener.onNewsUpload(true);
                }else{
                    onNewsListener.onNewsUpload(false);
                }

            }
        });
*/

    }

    public void downloadInitiativesList(int limit, final OnNewsListener onNewsListener) {


        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("Initiatives/");

        Query myref2 = mDatabaseRef.limitToLast(limit);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<News> newsArrayList = new ArrayList<News>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    News news = snapshot.getValue(News.class);

                    newsArrayList.add(news);
                }

                Collections.reverse(newsArrayList);

                onNewsListener.onNewsListDownload(newsArrayList, true);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });


    }

    public void downloadPIBSummaryList(int limit, final OnNewsListener onNewsListener) {


        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("Summary/");

        Query myref2 = mDatabaseRef.limitToLast(limit);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<News> newsArrayList = new ArrayList<News>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    News news = snapshot.getValue(News.class);

                    news.setNewsID(snapshot.getKey());
                    newsArrayList.add(news);
                }

                Collections.reverse(newsArrayList);

                onNewsListener.onNewsListDownload(newsArrayList, true);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });


    }


    public void downloadPIBSummaryList(int limitTo, String lastID, final OnNewsListener onNewsListener) {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Summary/");

        Query query = database.orderByKey().limitToLast(limitTo).endAt(lastID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<News> newsArrayList = new ArrayList<News>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    News news = snapshot.getValue(News.class);

                    news.setNewsID(snapshot.getKey());
                    newsArrayList.add(news);
                }


                Collections.reverse(newsArrayList);

                if (newsArrayList.size() > 1) {
                    newsArrayList.remove(0);
                }

                onNewsListener.onNewsListDownload(newsArrayList, true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);
            }
        });


    }


    public void downloadPIBSummaryById(String summaryId, final OnNewsListener onNewsListener) {


        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("Summary/" + summaryId);


        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<News> newsArrayList = new ArrayList<News>();

                News news = dataSnapshot.getValue(News.class);

                newsArrayList.add(news);

                onNewsListener.onNewsListDownload(newsArrayList, true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });


    }


    public void downloadPIBSummary(long startTimeInMillis, long endTimeInMillis, final OnNewsListener onNewsListener) {

        DatabaseReference myRef = mFirebaseDatabase.getReference("Summary/");
        Query myref2 = myRef.orderByChild("timeInMillis").startAt(startTimeInMillis).endAt(endTimeInMillis);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<News> newsArrayList = new ArrayList<News>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    News news = snapshot.getValue(News.class);

                    news.setNewsID(snapshot.getKey());
                    newsArrayList.add(news);
                }


                onNewsListener.onNewsListDownload(newsArrayList, true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });

    }

    public void downloadOtherNewsById(String otherNewsId, final OnNewsListener onNewsListener) {
        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("otherNews/" + otherNewsId);


        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<News> newsArrayList = new ArrayList<News>();


                News news = dataSnapshot.getValue(News.class);

                newsArrayList.add(news);


                onNewsListener.onNewsListDownload(newsArrayList, true);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });
    }

    public void downloadOtherNewsList(int limit, final OnNewsListener onNewsListener) {
        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("otherNews/");

        Query myref2 = mDatabaseRef.limitToLast(limit);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<News> newsArrayList = new ArrayList<News>();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    News news = snapshot.getValue(News.class);
                    if (news != null) {

                        news.setNewsID(snapshot.getKey());

                    }
                    newsArrayList.add(news);
                }

                Collections.reverse(newsArrayList);

                onNewsListener.onNewsListDownload(newsArrayList, true);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });
    }


    public void downloadStoryList(int limit, String lastShortStoryID, final OnNewsListener onNewsListener) {


        DatabaseReference mDatabaseRef = mFirebaseDatabase.getReference().child("news/");

        Query myref2 = mDatabaseRef.orderByKey().limitToLast(limit).endAt(lastShortStoryID);

        myref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<News> newsArrayList = new ArrayList<News>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    News news = snapshot.getValue(News.class);
                    if (news != null) {
                        news.setNewsID(snapshot.getKey());
                    }
                    newsArrayList.add(news);
                }

                newsArrayList.remove(newsArrayList.size() - 1);
                Collections.reverse(newsArrayList);
                onNewsListener.onNewsListDownload(newsArrayList, true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onNewsListener.onNewsListDownload(null, false);

            }
        });


    }


    public interface OnNewsListener {


        public void onNewsListDownload(ArrayList<News> newsArrayList, boolean isSuccessful);


        public void onNewsUpload(boolean isSuccessful);
    }

}
