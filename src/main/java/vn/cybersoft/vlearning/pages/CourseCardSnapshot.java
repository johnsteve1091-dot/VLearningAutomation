package vn.cybersoft.vlearning.pages;

public record CourseCardSnapshot(
        String name,
        String href,
        int cardIndex,
        boolean hasSubCard,
        ParentContent parent,
        SubCardContent subCard
) {

    public record ParentContent(
            String courseName,
            String instructorName,
            String instructorAvatarSrc,
            String hours,
            String weeks,
            String level
    ) {
    }

    public record SubCardContent(
            String courseName,
            String instructorName,
            String instructorAvatarSrc,
            String description,
            String hours,
            String weeks,
            String level,
            String detailButtonText
    ) {
    }
}