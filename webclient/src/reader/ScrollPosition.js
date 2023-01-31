const topPositions = [];

export function setTopPosition(index, position) {
    topPositions[index] = position;
}

export function getScrollPosition() {
    console.log('getScrollPosition: ', topPositions);
    const contentElement = document.getElementById('content');
    const scrollPosition = contentElement.scrollTop;
    console.log('scrollPosition', scrollPosition);

    const firstParagraphIndex = topPositions.findIndex(position => position >= scrollPosition);
    return `${firstParagraphIndex}:${topPositions[firstParagraphIndex] - scrollPosition}`;
}

export function parsePosition(positionStr) {
    const [indexStr, offsetStr] = positionStr.split(':');
    return {
        index: parseInt(indexStr),
        offset: parseInt(offsetStr)
    };
}