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

package com.lloydtorres.stately.telegrams;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;
import com.lloydtorres.stately.R;
import com.lloydtorres.stately.dto.Telegram;
import com.lloydtorres.stately.helpers.SparkleHelper;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-03-09.
 * An adapter used for displaying telegrams. Can be used for previews and full telegrams.
 */
public class TelegramsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int EMPTY_INDICATOR = -1;

    // constants for the different types of cards
    private final int PREVIEW_CARD = 0;
    private final int FULL_CARD = 1;
    private final int EMPTY_CARD = 2;

    private Context context;
    private List<Telegram> telegrams;

    public TelegramsAdapter(Context c, List<Telegram> t)
    {
        context = c;
        setTelgrams(t);
    }

    /**
     * Sets the contents of this telegram adapter.
     * @param t List of telegrams
     */
    public void setTelgrams(List<Telegram> t)
    {
        telegrams = t;
        if (telegrams.size() <= 0)
        {
            Telegram empty = new Telegram();
            empty.id = EMPTY_INDICATOR;
            telegrams.add(empty);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case FULL_CARD:
                View fullCard = inflater.inflate(R.layout.card_telegram, parent, false);
                viewHolder = new TelegramCard(context, fullCard);
                break;
            case PREVIEW_CARD:
                View previewCard = inflater.inflate(R.layout.card_telegram_preview, parent, false);
                viewHolder = new TelegramPreviewCard(context, previewCard);
                break;
            default:
                View emptyCard = inflater.inflate(R.layout.card_post, parent, false);
                viewHolder = new NoTelegramsCard(context, emptyCard);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case FULL_CARD:
                TelegramCard telegramCard = (TelegramCard) holder;
                telegramCard.init(telegrams.get(position));
                break;
            case PREVIEW_CARD:
                TelegramPreviewCard telegramPreviewCard = (TelegramPreviewCard) holder;
                telegramPreviewCard.init(telegrams.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return telegrams.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (telegrams.get(position).content != null)
        {
            return FULL_CARD;
        }
        if (telegrams.get(position).preview != null)
        {
            return PREVIEW_CARD;
        }
        else
        {
            return EMPTY_CARD;
        }
    }

    public void setAlertState(int type, RelativeLayout holder, ImageView icon, TextView text)
    {
        if (type != Telegram.TELEGRAM_GENERIC)
        {
            holder.setVisibility(View.VISIBLE);

            int iconRes = R.drawable.ic_alert_recruitment;
            int alertColor = R.color.colorChart1;
            int alertContent = R.string.telegrams_alert_recruitment;

            switch (type)
            {
                case Telegram.TELEGRAM_REGION:
                    iconRes = R.drawable.ic_region_green;
                    alertColor = R.color.colorChart3;
                    alertContent = R.string.telegrams_alert_region;
                    break;
                case Telegram.TELEGRAM_MODERATOR:
                    iconRes = R.drawable.ic_alert_moderator;
                    alertColor = R.color.colorChart3;
                    alertContent = R.string.telegrams_alert_mod;
                    break;
            }

            icon.setImageResource(iconRes);
            text.setTextColor(ContextCompat.getColor(context, alertColor));
            text.setText(context.getString(alertContent));
        }
        else
        {
            holder.setVisibility(View.GONE);
        }
    }

    public class TelegramCard extends RecyclerView.ViewHolder {

        // @TODO

        public TelegramCard(Context c, View v) {
            super(v);
        }

        public void init(Telegram t)
        {

        }
    }

    public class TelegramPreviewCard extends RecyclerView.ViewHolder {

        private Context context;
        private TextView header;
        private TextView timestamp;
        private RelativeLayout alertHolder;
        private ImageView alertIcon;
        private TextView alertText;
        private HtmlTextView preview;

        public TelegramPreviewCard(Context c, View v) {
            super(v);
            context = c;
            header = (TextView) v.findViewById(R.id.card_telegram_preview_from);
            timestamp = (TextView) v.findViewById(R.id.card_telegram_preview_time);
            alertHolder = (RelativeLayout) v.findViewById(R.id.card_telegram_preview_alert_holder);
            alertIcon = (ImageView) v.findViewById(R.id.card_telegram_preview_alert_icon);
            alertText = (TextView) v.findViewById(R.id.card_telegram_preview_alert_message);
            preview = (HtmlTextView) v.findViewById(R.id.card_telegram_preview_content);
        }

        public void init(Telegram t)
        {
            List<String> headerContents = new ArrayList<String>();
            headerContents.add(t.sender);
            if (t.recepients != null)
            {
                headerContents.addAll(t.recepients);
            }
            SparkleHelper.setHappeningsFormatting(context, header, Joiner.on(", ").skipNulls().join(headerContents));
            timestamp.setText(SparkleHelper.getReadableDateFromUTC(t.timestamp));
            setAlertState(t.type, alertHolder, alertIcon, alertText);
            preview.setText(SparkleHelper.getHtmlFormatting(t.preview).toString());
        }
    }

    public class NoTelegramsCard extends RecyclerView.ViewHolder {
        public NoTelegramsCard(Context c, View v)
        {
            super(v);
            TextView cardAuthor = (TextView) v.findViewById(R.id.card_post_name);
            TextView cardTime = (TextView) v.findViewById(R.id.card_post_time);
            HtmlTextView cardContent = (HtmlTextView) v.findViewById(R.id.card_post_content);
            cardTime.setVisibility(View.GONE);
            cardAuthor.setVisibility(View.GONE);
            cardContent.setText(c.getString(R.string.rmb_no_content));
            cardContent.setTypeface(cardContent.getTypeface(), Typeface.ITALIC);
        }
    }
}
