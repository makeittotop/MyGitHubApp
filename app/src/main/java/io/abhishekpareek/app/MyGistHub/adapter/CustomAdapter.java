package io.abhishekpareek.app.MyGistHub.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.abhishekpareek.app.MyGistHub.R;
import io.abhishekpareek.app.MyGistHub.models.MyGist;

/**
 * Created by apareek on 5/17/16.
 */
public class CustomAdapter extends BaseAdapter implements Filterable {
    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    private Activity activity;
    private List<MyGist> myGists = new ArrayList<>();

    private List<MyGist> filterable_myGists = new ArrayList<>();
    private ItemFilter mFilter = new ItemFilter();

    private static LayoutInflater inflater = null;
    private Resources res;


    public void setData(List<MyGist> gists) {
        this.myGists = gists;
        this.filterable_myGists = gists;

        notifyDataSetChanged();
    }

    public CustomAdapter(Activity a, List<MyGist> myGists, Resources r) {
        this.activity = a;
        this.myGists = myGists;
        this.filterable_myGists = myGists;
        this.res = r;

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (filterable_myGists.size() < 0)
            return 1;
        return filterable_myGists.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return filterable_myGists.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    public void filter(String type, String constraint) {
        switch (type) {
            case "update":
                String filterString = constraint.toString().toLowerCase();

                final List<MyGist> list = myGists;

                int count = list.size();
                final ArrayList<MyGist> nlist = new ArrayList<MyGist>(count);

                String filterableString ;

                for (int i = 0; i < count; i++) {
                    MyGist myGist = list.get(i);
                    // gist title
                    filterableString = myGist.getGistId();
                    if (filterableString.toLowerCase() == filterString) {
                        //////////Log.d("FILTER-MATCH", filterableString + " " + filterString);

                        nlist.add(myGist);
                    } else {
                        // gist description
                        filterableString = myGist.getGistDescription();
                        if (filterableString.toLowerCase().contains(filterString)) {
                            //////////Log.d("FILTER-MATCH", filterableString + " " + filterString);
                            nlist.add(myGist);
                        }
                    }
                }
                notifyDataSetChanged();
                break;
            case "delete":

                notifyDataSetChanged();
                break;
            case "create":

                notifyDataSetChanged();
                break;
        }
    }

    /**
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p/>
     * <p>This method is usually implemented by {@link Adapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {

        /**
         * <p>Invoked in a worker thread to filter the data according to the
         * constraint. Subclasses must implement this method to perform the
         * filtering operation. Results computed by the filtering operation
         * must be returned as a {@link FilterResults} that
         * will then be published in the UI thread through
         * {@link #publishResults(CharSequence,
         * FilterResults)}.</p>
         * <p/>
         * <p><strong>Contract:</strong> When the constraint is null, the original
         * data must be restored.</p>
         *
         * @param constraint the constraint used to filter the data
         * @return the results of the filtering operation
         * @see #filter(CharSequence, FilterListener)
         * @see #publishResults(CharSequence, FilterResults)
         * @see FilterResults
         */

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<MyGist> list = myGists;

            int count = list.size();
            final ArrayList<MyGist> nlist = new ArrayList<MyGist>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                MyGist myGist = list.get(i);
                // gist title
                filterableString = myGist.getGistTitle();
                if (filterableString.toLowerCase().contains(filterString)) {
                    //////////Log.d("FILTER-MATCH", filterableString + " " + filterString);
                    nlist.add(myGist);
                } else {
                    // gist description
                    filterableString = myGist.getGistDescription();
                    if (filterableString.toLowerCase().contains(filterString)) {
                        //////////Log.d("FILTER-MATCH", filterableString + " " + filterString);
                        nlist.add(myGist);
                    }
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        /**
         * <p>Invoked in the UI thread to publish the filtering results in the
         * user interface. Subclasses must implement this method to display the
         * results computed in {@link #performFiltering}.</p>
         *
         * @param constraint the constraint used to filter the data
         * @param results    the results of the filtering operation
         * @see #filter(CharSequence, FilterListener)
         * @see #performFiltering(CharSequence)
         * @see FilterResults
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filterable_myGists = (ArrayList<MyGist>) results.values;
            notifyDataSetChanged();
        }
    }
    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{
        public TextView gist_title;
        public TextView gist_type;
        public TextView gist_update_time;
        public ImageView star_iv;
        public  ImageView unstar_iv;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;

        if (convertView==null) {
            vi = inflater.inflate(R.layout.row_gist_list_view, null);

            holder = new ViewHolder();
            holder.gist_title = (TextView) vi.findViewById(R.id.gist_title_tv);
            holder.gist_type = (TextView) vi.findViewById(R.id.gist_type_tv);
            holder.gist_update_time = (TextView) vi.findViewById(R.id.gist_update_time_tv);
            holder.star_iv = (ImageView) vi.findViewById(R.id.star_iv);
            holder.unstar_iv = (ImageView) vi.findViewById(R.id.not_star_iv);

            vi.setTag(holder);

            /*
            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //////////Log.d("foo", "bar2");
                }
            });
            */
        } else
            holder = (ViewHolder) vi.getTag();

        if (filterable_myGists.size() <= 0)
            holder.gist_title.setText("No Gists");
        else {
            MyGist g = filterable_myGists.get(position);

            holder.gist_title.setText(g.getGistTitle());
            if (g.getGistTypePublic())
                holder.gist_type.setText("Public");
            else
                holder.gist_type.setText("Private");
            holder.gist_update_time.setText(g.getGistUpdatedAt());
            if (g.isStarred()) {
                holder.star_iv.setVisibility(View.VISIBLE);
                holder.unstar_iv.setVisibility(View.GONE);
            } else {
                holder.star_iv.setVisibility(View.GONE);
                holder.unstar_iv.setVisibility(View.VISIBLE);
            }
        }

        return vi;
    }
}
