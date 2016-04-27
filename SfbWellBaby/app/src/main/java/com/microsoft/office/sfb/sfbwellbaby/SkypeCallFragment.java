package com.microsoft.office.sfb.sfbwellbaby;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.microsoft.media.MMVRSurfaceView;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.MessageActivityItem;
import com.microsoft.office.sfb.appsdk.ParticipantService;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.helpers.ConversationHelper;


/**
 * A placeholder fragment containing a simple view.
 */
public class SkypeCallFragment extends Fragment
        implements ConversationHelper.ConversationCallback {

    public Button mPauseButton;
    public Button mEndCallButton;
    public Button mMuteAudioButton;
    private OnFragmentInteractionListener mListener;
    private static Conversation mConversation;
    private static DevicesManager mDevicesManager;
    private ConversationHelper mConversationHelper;
    private MMVRSurfaceView mParticipantVideoSurfaceView;

    View mRootView;


    @SuppressLint("ValidFragment")
    public SkypeCallFragment() {
    }

    /**
     * Create the Video fragment.
     *
     * @return A new instance of fragment VideoFragment.
     */
    public static SkypeCallFragment newInstance(
            Conversation conversation,
            DevicesManager devicesManager) {
        mConversation = conversation;
        mDevicesManager = devicesManager;
        SkypeCallFragment fragment = new SkypeCallFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_skype_call, container, false);
        mEndCallButton = (Button) mRootView.findViewById(R.id.endCallButton);
        mEndCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConversation.canLeave())
                    try {
                        mConversation.leave();
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
            }
        });

        mMuteAudioButton = (Button) mRootView.findViewById(R.id.muteAudioButton);
        mMuteAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConversationHelper.toggleMute();
            }
        });
        Log.i(
                "SkypeCallFragment",
                "onCreateView ");
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextureView previewVideoTextureView = (TextureView) mRootView.findViewById(
                R.id.selfParticipantVideoView);
        View participantVideoLayout = (View) mRootView.findViewById(
                R.id.participantVideoLayoutId);
        mParticipantVideoSurfaceView = new MMVRSurfaceView(
                participantVideoLayout.getContext());
        mListener.onFragmentInteraction(mRootView
                , getActivity()
                        .getString(
                                R.string.callFragmentInflated));
        mConversationHelper = new ConversationHelper(
                mConversation,
                mDevicesManager,
                previewVideoTextureView,
                mParticipantVideoSurfaceView,
                this);
        Log.i(
                "SkypeCallFragment",
                "onViewCreated");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    /**
     * Used to interact with parent activity
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(View rootView, String newMeetingURI);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mListener != null) {
            mListener.onFragmentInteraction(
                    mRootView,
                    getActivity().
                            getString(R.string.leaveCall));
            mListener = null;
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    public void onDetach() {
        super.onDetach();
        //mCalled = true;
    }


    @Override
    public void onConversationStateChanged(Conversation.State state) {
        Log.i(
                "SkypeCallFragment",
                "onConversationStateChanged "
                        + String.valueOf(state));

        if (state == Conversation.State.IDLE) {
            if (mListener != null) {
                mListener.onFragmentInteraction(
                        mRootView,
                        getActivity().
                                getString(R.string.leaveCall));
                mListener = null;
            }
        }
    }

    @Override
    public void onCanSendMessage(boolean b) {
        Log.i(
                "SkypeCallFragment",
                "onCanSendMessage "
                        + String.valueOf(b));
    }

    @Override
    public void onMessageReceived(MessageActivityItem messageActivityItem) {

    }

    @Override
    public void onSelfAudioStateChanged(ParticipantService.State state) {

        Log.i(
                "SkypeCallFragment",
                "onSelfAudioStateChanged "
                        + String.valueOf(state));
    }

    @Override
    public void onSelfAudioMuteChanged(final boolean b) {

        Log.i(
                "SkypeCallFragment",
                "onSelfAudioMuteChanged "
                        + String.valueOf(b));

        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (b == true) {
                        mMuteAudioButton.setText("Unmute");
                    } else {
                        mMuteAudioButton.setText("Mute");
                    }
                }
            });
        } catch (Exception e) {
            Log.e("SkypeCall", "exception on meeting started");
        }
    }

    @Override
    public void onCanStartVideoServiceChanged(boolean b) {
        Log.i(
                "SkypeCallFragment",
                "onCanStartVideoServiceChanged "
                        + String.valueOf(b));

        if (b == true) {
            mConversationHelper.startOutgoingVideo();
            mConversationHelper.startIncomingVideo();
        }
    }

    @Override
    public void onCanSetActiveCameraChanged(boolean b) {
        Log.i(
                "SkypeCallFragment",
                "onCanSetActiveCameraChanged "
                        + String.valueOf(b));

        if (b == true) {
            mConversationHelper.changeActiveCamera();
        }
    }
}
