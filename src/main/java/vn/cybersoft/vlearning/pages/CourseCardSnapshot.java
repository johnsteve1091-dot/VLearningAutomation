package vn.cybersoft.vlearning.pages;

public record CourseCardSnapshot(
        String name,
        String href,
        int cardIndex,
        boolean hasSubCard
) {
}