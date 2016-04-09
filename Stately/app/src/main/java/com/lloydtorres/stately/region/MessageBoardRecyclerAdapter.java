/**
 * Copyright 2016 Lloyd Torres
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lloydtorres.stately.region;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Post;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.List;

/**
 * Created by Lloyd on 2016-01-24.
 * An adapter for the recyclerview in MessageBoardActivity.
 */
public class MessageBoardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int NO_SELECTION = -1;
    private static final String DELETED_CONTENT = "Message deleted by author";
    private static final int EMPTY_INDICATOR = -1;

    private Context context;
    private List<Post> messages;
    private int replyIndex = NO_SELECTION;
    private boolean isPostable = false;

    public MessageBoardRecyclerAdapter(Context c, List<Post> p, boolean ec)
    {
        context = c;
        isPostable = ec;
        setMessages(p);
    }

    /**
     * Set new messages
     * @param p List of posts
     */
    public void setMessages(List<Post> p)
    {
        messages = p;

        if (messages.size() <= 0)
        {
            Post np = new Post();
            np.id = EMPTY_INDICATOR;
            messages.add(np);
        }
        notifyDataSetChanged();
    }

    /**
     * Set which message to reply to
     * @param i Index
     */
    public void setReplyIndex(int i)
    {
        int oldReplyIndex = replyIndex;
        replyIndex = i;

        if (oldReplyIndex != -1)
        {
            notifyItemChanged(oldReplyIndex);
        }
        if (replyIndex != -1)
        {
            notifyItemChanged(replyIndex);
        }

        if (replyIndex == oldReplyIndex)
        {
            replyIndex = NO_SELECTION;
            notifyItemChanged(oldReplyIndex);
        }
    }

    /**
     * Add an offset to the reply index
     * @param a Offset
     */
    public void addToReplyIndex(int a)
    {
        if (replyIndex != -1)
        {
            setReplyIndex(replyIndex + a);
        }
    }

    /**
     * Mark a message as having been deleted
     * @param i
     */
    public void setAsDeleted(int i)
    {
        messages.get(i).message = DELETED_CONTENT;
        messages.get(i).status = Post.POST_DELETED;
        notifyItemChanged(i);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View postCard = inflater.inflate(R.layout.card_post, parent, false);
        RecyclerView.ViewHolder viewHolder = new PostCard(context, postCard);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PostCard postCard = (PostCard) holder;
        Post message = messages.get(position);
        postCard.init(message);

        if (position == replyIndex)
        {
            postCard.select();
        }
        else
        {
            postCard.deselect();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class PostCard extends RecyclerView.ViewHolder {

        private Context context;
        private Post post;
        private CardView cardContainer;
        private TextView cardAuthor;
        private TextView cardTime;
        private HtmlTextView cardContent;
        private RelativeLayout actionsHolder;
        private ImageView likeButton;
        private TextView likeCount;
        private ImageView deleteButton;
        private ImageView replyButton;

        private View.OnClickListener replyClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && post.message != null)
                {
                    if (replyIndex == pos)
                    {
                        ((MessageBoardActivity) context).setReplyMessage(null);
                    }
                    else
                    {
                        ((MessageBoardActivity) context).setReplyMessage(post, pos);
                        setReplyIndex(pos);
                    }
                }
            }
        };

        private View.OnClickListener deleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                {
                    ((MessageBoardActivity) context).confirmDelete(pos, post.id);
                }
            }
        };

        public PostCard(Context c, View v) {
            super(v);
            context = c;
            cardContainer = (CardView) v.findViewById(R.id.card_post_container);
            cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            cardTime = (TextView) v.findViewById(R.id.card_post_time);
            cardContent = (HtmlTextView) v.findViewById(R.id.card_post_content);
            actionsHolder = (RelativeLayout) v.findViewById(R.id.card_post_actions_holder);
            likeButton = (ImageView) v.findViewById(R.id.card_post_like);
            likeCount = (TextView) v.findViewById(R.id.card_post_like_count);
            deleteButton = (ImageView) v.findViewById(R.id.card_post_delete);
            replyButton = (ImageView) v.findViewById(R.id.card_post_reply);

            actionsHolder.setVisibility(isPostable ? View.VISIBLE : View.GONE);
        }

        public void init(Post p)
        {
            post = p;
            if (post.id != EMPTY_INDICATOR)
            {
                SparkleHelper.activityLinkBuilder(context, cardAuthor, post.name, post.name, SparkleHelper.getNameFromId(post.name), SparkleHelper.CLICKY_NATION_MODE);
                cardTime.setText(SparkleHelper.getReadableDateFromUTC(context, post.timestamp));
                String postContent = post.message;
                if (post.status == Post.POST_SUPPRESSED && post.suppressor != null)
                {
                    postContent = String.format(context.getString(R.string.rmb_suppressed), post.suppressor) + "<strike>" + postContent + "</strike>";
                }
                if (post.status == Post.POST_DELETED || post.status == Post.POST_BANHAMMERED)
                {
                    postContent = "[i]" + postContent + "[/i]";
                }
                SparkleHelper.setBbCodeFormatting(context, cardContent, postContent);

                // Setup actions holder
                if (isPostable && (post.status == Post.POST_REGULAR || post.status == Post.POST_SUPPRESSED))
                {
                    actionsHolder.setVisibility(View.VISIBLE);
                    // All posts can be replied to
                    replyButton.setOnClickListener(replyClickListener);
                    // Only user's own posts can be deleted
                    if (context != null && SparkleHelper.getActiveUser(context).nationId.equals(post.name))
                    {
                        deleteButton.setVisibility(View.VISIBLE);
                        deleteButton.setOnClickListener(deleteClickListener);
                    }
                    else
                    {
                        deleteButton.setVisibility(View.GONE);
                        deleteButton.setOnClickListener(null);
                    }
                    // @TODO: Like button and like list
                }
                else
                {
                    actionsHolder.setVisibility(View.GONE);
                }
            }
            else
            {
                cardTime.setVisibility(View.GONE);
                cardAuthor.setVisibility(View.GONE);
                cardContent.setText(context.getString(R.string.rmb_no_content));
                cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 0);
                cardContent.setLayoutParams(params);
                actionsHolder.setVisibility(View.GONE);
            }
        }

        public void select()
        {
            cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.highlightColor));
            replyButton.setImageResource(R.drawable.ic_clear);
        }

        public void deselect()
        {
            cardContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            replyButton.setImageResource(R.drawable.ic_reply);
        }
    }

}
