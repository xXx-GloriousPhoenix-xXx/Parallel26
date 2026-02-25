package journal;

import org.w3c.dom.ranges.RangeException;

import java.util.ArrayList;
import java.util.Random;

public class DataFactory {
    private final int groupCount;
    private final int studentCount;
    private final Random random = new Random();

    public DataFactory(int groupCount, int studentCount) {
        this.groupCount = groupCount;
        this.studentCount = studentCount;
    }

    public String getGroup(int value) {
        if (value < 0 || value > groupCount)
            throw new RangeException((short) 1,
                    "value поза діапазоном кількості груп або менше 0");
        return String.format("ІП-3%d", value);
    }

    public String getName(int value) {
        if (value < 0 || value > studentCount)
            throw new RangeException((short) 1,
                    "value поза діапазоном кількості студентів або менше 0");
        return String.format("Ім`я %d", value);
    }

    public GradeItem getGrade() {
        return new GradeItem(
                getGroup(random.nextInt(groupCount) + 1),
                getName(random.nextInt(studentCount) + 1),
                random.nextInt(101)
        );
    }

    public ArrayList<GradeItem> getGradeList(int value) {
        var array = new ArrayList<GradeItem>();
        for (var i = 0; i < value; i++) {
            array.add(getGrade());
        }
        return array;
    }
}
