package com.edeqa.waytous.holders.view;

import android.content.SharedPreferences;
import android.graphics.Rect;
import androidx.appcompat.widget.Toolbar;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.getkeepsafe.taptargetview.TapTarget;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created 12/31/16.
 */
@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
public class IntroViewHolder extends AbstractViewHolder {
    private static final String TYPE = "intro";

    private final SharedPreferences preferences;
    private HashMap<String, ArrayList<IntroRule>> queue;
    private ShowcaseView sv;


    @SuppressWarnings("unused")
    public IntroViewHolder(MainActivity context) {
        super(context);

        preferences = context.getSharedPreferences("intro", MODE_PRIVATE);

        Map<String, AbstractViewHolder> holders = State.getInstance().getSystemViewBus().getHolders();
        queue = new HashMap<>();
        for(Map.Entry<String, AbstractViewHolder> entry: holders.entrySet()) {
            if(entry.getValue() == null) continue;
            //noinspection unchecked
            ArrayList<IntroRule> rules = entry.getValue().getIntro();
            if(rules == null) continue;

            for(IntroRule x: rules) {
                if(x.getEvent() != null) {
                    String event = x.getEvent();
                    ArrayList<IntroRule> eventRules;
                    if(queue.containsKey(event)){
                        eventRules = queue.get(event);
                    } else {
                        eventRules = new ArrayList<>();
                        queue.put(event, eventRules);
                    }
                    eventRules.add(x);
                }
            }
        }
    }

    private void performRules(ArrayList<IntroRule> eventQueue) {
        performRule(eventQueue, 0);
    }

    private void performRule(final ArrayList<IntroRule> eventQueue, final int order) {
        if (eventQueue == null || eventQueue.size() <= order) return;
        final IntroRule rule = eventQueue.get(order);
        if (rule == null) return;

        rule.setOrder(order);
        if(!preferences.getBoolean(rule.getId(), false)){
            TapTarget action = null;
            View view = rule.getView();
            Rect rect = null;
            String title = rule.getTitle();
            if(title == null) title = "";
            if(rule.getViewId() > 0) {
                view = context.findViewById(rule.getViewId());
            }
            if(rule.getLinkTo() > 0){
                switch(rule.getLinkTo()) {
                    case IntroRule.LINK_TO_CENTER:
                        rect = new Rect(0, 0, 100, 100);
                        // Using deprecated methods makes you look way cool
                        Display display = context.getWindowManager().getDefaultDisplay();
                        rect.offset(display.getWidth() / 2-50, display.getHeight() / 2);
                        break;
                    case IntroRule.LINK_TO_CENTER_OF_VIEW:
                        rect = new Rect(0, 0, 100, 100);
                        // Using deprecated methods makes you look way cool
//                    view = context.findViewById(R.layout.content_main).findViewWithTag(rule.getViewTag());
                        rect.offset(view.getWidth()/2-50, view.getHeight()/2+25);
                        break;
                    case IntroRule.LINK_TO_OPTIONS_MENU:
                        Toolbar toolbar = context.findViewById(R.id.toolbar);
                        action = TapTarget.forToolbarOverflow(toolbar, title, rule.getDescription()).transparentTarget(true);
                        break;
                    case IntroRule.LINK_TO_DRAWER_BUTTON:
                        toolbar = context.findViewById(R.id.toolbar);
                        action = TapTarget.forToolbarNavigationIcon(toolbar, title, rule.getDescription()).transparentTarget(true);
                        break;
                    case IntroRule.LINK_TO_OPTIONS_MENU_ITEM:
//                        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
//                        sequence.target(TapTarget.forToolbarMenuItem(toolbar, rule.getViewId(), rule.getTitle(), rule.getDescription()).transparentTarget(true));
                        break;
//                        View view1 = view.getRootView().findViewById(R.id.parentPanel);
//                        sequence.target(TapTarget.forToolbarMenuItem(toolbar, rule.getViewId(), rule.getTitle(), rule.getDescription()).transparentTarget(true));
//                        break;
                }
            }

            if(rect != null) {
//                action = TapTarget.forBounds(rect, title, rule.getDescription()).transparentTarget(true);
            } else if(view != null) {
                RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                int margin = ((Number) (context.getResources().getDisplayMetrics().density * 12)).intValue();
                lps.setMargins(margin, margin, margin, margin);

                ViewTarget target = new ViewTarget(view);
                sv = new ShowcaseView.Builder(context)
                        .withMaterialShowcase()
                        .setTarget(target)
                        .setContentTitle(title)
//                        .setStyle(R.style.ShowcaseView)
                        .setContentText(rule.getDescription())
                        .withMaterialShowcase()
                        .withNewStyleShowcase()
                        .hideOnTouchOutside()
                        .blockAllTouches()
                        .singleShot(view.getId())
//                        .setStyle(R.style.CustomShowcaseTheme2)
                        .setShowcaseEventListener(new OnShowcaseEventListener() {
                            @Override
                            public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                Utils.log(IntroViewHolder.this, "performRule:", "showcaseView");
                            }

                            @Override
                            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                preferences.edit().putBoolean(rule.getId(), true).apply();
//                                performRule(eventQueue, order + 1);
                                Utils.log(IntroViewHolder.this, "performRule:", "onShowcaseViewDidHide");
                            }

                            @Override
                            public void onShowcaseViewShow(ShowcaseView showcaseView) {
                                Utils.log(IntroViewHolder.this, "performRule:", "onShowcaseViewShow");
//                                showcaseView.setAlpha(0.0f);
                            }

                            @Override
                            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {
                                Utils.log(IntroViewHolder.this, "performRule:", "onShowcaseViewTouchBlocked");
                            }
                        })
//                        .replaceEndButton(R.layout.view_custom_button)
                        .build();
                sv.setButtonPosition(lps);
                sv.setBackgroundResource(R.drawable.semi_transparent_background);

//                action = TapTarget.forView(view, title, rule.getDescription()).transparentTarget(true);
            }
            if(action != null) {
                sv.show();
//                    TapTargetView.showFor(this.context, action, new TapTargetView.Listener() {
//                        @Override
//                        public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
//                            super.onTargetDismissed(view, userInitiated);
//                            preferences.edit().putBoolean(rule.getId(), true).apply();
//                            performRule(eventQueue, order + 1);
//                        }
//                    });
            } else {
                performRule(eventQueue, order + 1);
            }
        } else {
            performRule(eventQueue, order + 1);
        }
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        if(queue.containsKey(event)) {
            performRules(queue.get(event));
        }
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

}
/*
public class IntroViewHolder extends AbstractViewHolder {
    private static final String TYPE = "intro";

    private final AppCompatActivity context;
    private final SharedPreferences preferences;
    private HashMap<String, ArrayList<IntroRule>> queue;


    public IntroViewHolder(AppCompatActivity context) {
        System.out.println("INTRO:SYSTEMCONSTRUCT");
        this.context = context;

        preferences = context.getSharedPreferences("intro", MODE_PRIVATE);

        HashMap<String, AbstractViewHolder> holders = State.getInstance().getViewHolders();
        queue = new HashMap<>();
        for(Map.Entry<String, AbstractViewHolder> entry: holders.entrySet()) {
            if(entry.getValue() == null) continue;
            ArrayList<IntroRule> rules = entry.getValue().getIntro();
            if(rules == null) continue;

            for(IntroRule x: rules) {
                if(x.getEvent() != null) {
                    String event = x.getEvent();
                    ArrayList<IntroRule> eventRules;
                    if(queue.containsKey(event)){
                        eventRules = queue.get(event);
                    } else {
                        eventRules = new ArrayList<>();
                        queue.put(event, eventRules);
                    }
                    eventRules.put(x);
                }
            }
        }
    }

    private void performRules(ArrayList<IntroRule> eventQueue) {
        performRule(eventQueue, 0);
    }

    private void performRule(final ArrayList<IntroRule> eventQueue, final int order) {
        if (eventQueue == null || eventQueue.size() <= order) return;
        final IntroRule rule = eventQueue.get(order);
        if (rule == null) return;

        rule.setOrder(order);
        if(!preferences.getBoolean(rule.getId(), false)){
            System.out.println("PERFORM:"+this.context+":"+context);
            TapTarget action = null;
            View view = rule.getView();
            Rect rect = null;
            String title = rule.getTitle();
            if(title == null) title = "";
            if(rule.getViewId() > 0) {
                view = context.findViewById(rule.getViewId());
            } else if(rule.getViewTag() != null) {
//                view = context.findViewById(R.layout.content_main).findViewWithTag(rule.getViewTag());
            }
            if(rule.getLinkTo() > 0){
                switch(rule.getLinkTo()) {
                    case IntroRule.LINK_TO_CENTER:
                        rect = new Rect(0, 0, 100, 100);
                        // Using deprecated methods makes you look way cool
                        Display display = context.getWindowManager().getDefaultDisplay();
                        rect.offset(display.getWidth() / 2-50, display.getHeight() / 2);
                        break;
                    case IntroRule.LINK_TO_CENTER_OF_VIEW:
                        rect = new Rect(0, 0, 100, 100);
                        // Using deprecated methods makes you look way cool
//                    view = context.findViewById(R.layout.content_main).findViewWithTag(rule.getViewTag());
                        rect.offset(view.getWidth()/2-50, view.getHeight()/2+25);
                        break;
                    case IntroRule.LINK_TO_OPTIONS_MENU:
                        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
                        action = TapTarget.forToolbarOverflow(toolbar, title, rule.getDescription()).transparentTarget(true);
                        break;
                    case IntroRule.LINK_TO_DRAWER_BUTTON:
                        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
                        action = TapTarget.forToolbarNavigationIcon(toolbar, title, rule.getDescription()).transparentTarget(true);
                        break;
                    case IntroRule.LINK_TO_OPTIONS_MENU_ITEM:
//                        toolbar = (Toolbar) context.findViewById(R.id.toolbar);
//                        sequence.target(TapTarget.forToolbarMenuItem(toolbar, rule.getViewId(), rule.getTitle(), rule.getDescription()).transparentTarget(true));
                        break;
//                        View view1 = view.getRootView().findViewById(R.id.parentPanel);
//                        sequence.target(TapTarget.forToolbarMenuItem(toolbar, rule.getViewId(), rule.getTitle(), rule.getDescription()).transparentTarget(true));
//                        break;
                }
            }

            if(rect != null) {
                action = TapTarget.forBounds(rect, title, rule.getDescription()).transparentTarget(true);
            } else if(view != null) {
                action = TapTarget.forView(view, title, rule.getDescription()).transparentTarget(true);
            }
            if(action != null) {
                    TapTargetView.showFor(this.context, action, new TapTargetView.Listener() {
                        @Override
                        public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                            super.onTargetDismissed(view, userInitiated);
                            preferences.edit().putBoolean(rule.getId(), true).apply();
                            performRule(eventQueue, order + 1);
                        }
                    });
            } else {
                performRule(eventQueue, order + 1);
            }
        } else {
            performRule(eventQueue, order + 1);
        }
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        if(queue.containsKey(event)) {
            performRules(queue.get(event));
        }
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

}
*/
