package ru.wtg.whereaminow.holders;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.State.CHANGE_COLOR;
import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.MAKE_ACTIVE;
import static ru.wtg.whereaminow.State.MAKE_INACTIVE;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.UNSELECT_USER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;

/**
 * Created 11/18/16.
 */
public class PropertiesHolder extends AbstractPropertyHolder<PropertiesHolder.Properties> {
    public static final String TYPE = "properties";

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public String[] getOwnEvents() {
        return new String[0];
    }

    @Override
    public Properties create(MyUser myUser) {
        if (myUser == null) return null;
        return new Properties(myUser);
    }

    public class Properties extends AbstractProperty {
        private String name;
        private int number;
        private int color;
        private boolean selected;
        private boolean active;
        private boolean showTrack;

        Properties(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean dependsOnLocation(){
            return false;
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                    case SELECT_USER:
                        selected = true;
                        break;
                    case UNSELECT_USER:
                        selected = false;
                        break;
                    case MAKE_ACTIVE:
                        active = true;
                        break;
                    case MAKE_INACTIVE:
                        active = false;
                        break;
                    case CHANGE_NUMBER:
                        number = (int) object;
                        break;
                    case CHANGE_COLOR:
                        color = (int) object;
                        break;
                    case CHANGE_NAME:
                        name = (String) object;
                        if(myUser == State.getInstance().getMe()){
                            State.getInstance().setPreference("my_name",name);
                            if(State.getInstance().myTracking != null){
                                State.getInstance().myTracking.sendMessage(USER_NAME,name);
                            }
                        }
                        break;
            }
            return true;
        }

        public String getName() {
            return name;
        }

        public int getNumber() {
            return number;
        }

        public boolean isSelected() {
            return selected;
        }

        public boolean isActive() {
            return active;
        }

        public int getColor() {
            return color;
        }

        public boolean isShowTrack() {
            return showTrack;
        }

        public void setShowTrack(boolean showTrack) {
            this.showTrack = showTrack;
        }
    }

}
