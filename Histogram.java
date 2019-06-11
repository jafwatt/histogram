package com.jafwatt;

import java.util.*;

public class Histogram {

    private Long min;
    private Long max;
    private Category highestCount;
    private List<Category> categoryList;

    public class Category {

        private Long from;
        private Long to;
        private Long count;

        public Category(Long from, Long to) {
            this.from = from;
            this.to = to;
            this.count = 0L;
        }

        public boolean inRange(Long value) {
            return this.from <= value && value <= to;
        }

        public void increment() {
            this.count++;
        }

        @Override
        public String toString() {
            return String.format("%d-%d = %d", from, to, count);
        }

        public Long getFrom() {
            return from;
        }

        public Long getTo() {
            return to;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    public Histogram(List<Long> samples) {
        this(samples, null);
    }

    public Histogram(List<Long> samples, List<Long> categoryArray) {
        if (categoryArray == null) {
            this.min = samples.stream().mapToLong(v -> v).min().orElseThrow(NoSuchElementException::new);
            this.max = samples.stream().mapToLong(v -> v).max().orElseThrow(NoSuchElementException::new);
            this.categoryList = getCategoryList(min, max);
        } else {
            this.min = categoryArray.stream().mapToLong(v -> v).min().orElseThrow(NoSuchElementException::new);
            this.max = categoryArray.stream().mapToLong(v -> v).max().orElseThrow(NoSuchElementException::new);
            this.categoryList = getCategoryList(categoryArray);
        }
        if (categoryList.size() > 0) {
            samples.stream().forEach(v -> increment(categoryList, v));
            this.highestCount = categoryList.stream().max(Comparator.comparing(Category::getCount)).orElseThrow(NoSuchElementException::new);
        }
    }

    private void increment(List<Category> categoryList, Long value) {
        categoryList.stream().filter(c -> c.inRange(value)).findFirst().ifPresent(Category::increment);
    }

    private List<Category> getCategoryList(Long min, Long max) {
        List<Category> categoryList = new ArrayList<>();
        long range = max - min;
        long step = range / 10;
        long from = min;
        long to = from;
        while (to < max) {
            to = from + step;
            categoryList.add(new Category(from, to));
            from += step + 1;
        }
        return categoryList;
    }

    private List<Category> getCategoryList(List<Long> categoryBoundaryList) {
        Collections.sort(categoryBoundaryList);
        List<Category> categoryList = new ArrayList<>();
        Long lowerBoundary = 0L;
        for (int i = 0; i < categoryBoundaryList.size(); i++) {
            if (i > 0) {
                categoryList.add(new Category(lowerBoundary, categoryBoundaryList.get(i)));
                lowerBoundary = categoryBoundaryList.get(i) + 1;
            } else {
                lowerBoundary = categoryBoundaryList.get(i);
            }
        }
        return categoryList;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (categoryList.size() > 0) {
            long step = highestCount.getCount() / 40 > 0 ? highestCount.getCount() / 40 : 1;
            long valueLength = Long.toString(max).length();
            this.categoryList.forEach(category -> {
                s.append(getLeftPaddedValue(category.getFrom(), valueLength));
                s.append("-");
                s.append(getRightPaddedValue(category.getTo(), valueLength));
                s.append(" : ");
                s.append(getBar(category, step));
                s.append(" ");
                s.append(category.getCount());
                s.append("\n");
            });
        } else {
            s.append("No date to plot");
        }
        return s.toString();
    }

    private String getLeftPaddedValue(Long value, Long length) {
        return String.format("%" + length + "d", value);
    }

    private String getRightPaddedValue(Long value, Long length) {
        return String.format("%-" + length + "d", value);
    }

    private String getBar(Category category, Long step) {
        return new String(new char[category.getCount().intValue() / step.intValue() + 1]).replace("\0", "|");
    }
}
