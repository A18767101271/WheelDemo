package player.wislie.com.wheeldemo;

import java.util.ArrayList;
import java.util.List;

public class WheelAdapter<T> {

    private List<T> dataList = new ArrayList<>();

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList.addAll(dataList);
    }
}
