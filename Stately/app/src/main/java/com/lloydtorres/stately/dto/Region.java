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

package com.lloydtorres.stately.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.lloydtorres.stately.helpers.SparkleHelper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lloyd on 2016-01-21.
 * The DTO used to store information about a region, as returned by the NationStates API.
 * Excludes messages, which is stored in a separate DTO.
 */
@Root(name="REGION", strict=false)
public class Region implements Parcelable {

    public static final String QUERY = "https://www.nationstates.net/cgi-bin/api.cgi?region=%s&q="
                                        + "name+flag+numnations"
                                        + "+delegate+delegatevotes+founder+founded+power"
                                        + "+factbook+tags"
                                        + "+poll+gavote+scvote"
                                        + "+officers+embassies"
                                        + "+happenings+history"
                                        + "+census"
                                        + ";scale=all;mode=score+rank+prank"
                                        + "&v=" + SparkleHelper.API_VERSION;
    public static final String GET_QUERY = "https://www.nationstates.net/region=%s";
    public static final String QUERY_HTML = "https://www.nationstates.net/region=%s/template-overall=none";
    public static final String CHANGE_QUERY = "https://www.nationstates.net/page=change_region/template-overall=none";

    @Element(name="NAME")
    public String name;
    @Element(name="FLAG", required=false)
    public String flagURL;
    @Element(name="NUMNATIONS")
    public int numNations;

    @Element(name="DELEGATE")
    public String delegate;
    @Element(name="DELEGATEVOTES")
    public int delegateVotes;
    @Element(name="FOUNDER")
    public String founder;
    @Element(name="FOUNDED")
    public String founded;
    @Element(name="POWER")
    public String power;

    @Element(name="FACTBOOK", required=false)
    public String factbook;
    @ElementList(name="TAGS")
    public List<String> tags;

    @Element(name="POLL", required=false)
    public Poll poll;
    @Element(name="GAVOTE", required=false)
    public WaVote gaVote;
    @Element(name="SCVOTE", required=false)
    public WaVote scVote;

    @ElementList(name="OFFICERS", required=false)
    public List<Officer> officers;
    @ElementList(name="EMBASSIES", required=false)
    public List<Embassy> embassies;

    @ElementList(name="CENSUS")
    public List<CensusDetailedRank> census;

    @ElementList(name="HAPPENINGS")
    public List<Event> happenings;
    @ElementList(name="HISTORY")
    public List<Event> history;

    public Region() { super(); }

    protected Region(Parcel in) {
        name = in.readString();
        flagURL = in.readString();
        numNations = in.readInt();
        delegate = in.readString();
        delegateVotes = in.readInt();
        founder = in.readString();
        founded = in.readString();
        power = in.readString();
        factbook = in.readString();
        if (in.readByte() == 0x01) {
            tags = new ArrayList<String>();
            in.readList(tags, String.class.getClassLoader());
        } else {
            tags = null;
        }
        poll = (Poll) in.readValue(Poll.class.getClassLoader());
        gaVote = (WaVote) in.readValue(WaVote.class.getClassLoader());
        scVote = (WaVote) in.readValue(WaVote.class.getClassLoader());
        if (in.readByte() == 0x01) {
            officers = new ArrayList<Officer>();
            in.readList(officers, Officer.class.getClassLoader());
        } else {
            officers = null;
        }
        if (in.readByte() == 0x01) {
            embassies = new ArrayList<Embassy>();
            in.readList(embassies, Embassy.class.getClassLoader());
        } else {
            embassies = null;
        }
        if (in.readByte() == 0x01) {
            census = new ArrayList<CensusDetailedRank>();
            in.readList(census, CensusDetailedRank.class.getClassLoader());
        } else {
            census = null;
        }
        if (in.readByte() == 0x01) {
            happenings = new ArrayList<Event>();
            in.readList(happenings, Event.class.getClassLoader());
        } else {
            happenings = null;
        }
        if (in.readByte() == 0x01) {
            history = new ArrayList<Event>();
            in.readList(history, Event.class.getClassLoader());
        } else {
            history = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(flagURL);
        dest.writeInt(numNations);
        dest.writeString(delegate);
        dest.writeInt(delegateVotes);
        dest.writeString(founder);
        dest.writeString(founded);
        dest.writeString(power);
        dest.writeString(factbook);
        if (tags == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(tags);
        }
        dest.writeValue(poll);
        dest.writeValue(gaVote);
        dest.writeValue(scVote);
        if (officers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(officers);
        }
        if (embassies == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(embassies);
        }
        if (census == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(census);
        }
        if (happenings == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(happenings);
        }
        if (history == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(history);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>() {
        @Override
        public Region createFromParcel(Parcel in) {
            return new Region(in);
        }

        @Override
        public Region[] newArray(int size) {
            return new Region[size];
        }
    };
}
