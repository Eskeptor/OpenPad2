package com.esk.openpadnew.DataType

import java.util.*

/**
 * 원 오브젝트(원형 그리기)
 * @param x x좌표
 * @param y y좌표
 * @param r 반지름
 */
class CircleObject(val x: Float, val y: Float, val r: Float)


/**
 * 일반 브러쉬 오브젝트
 */
class BrushObject {
    /**
     * 브러쉬 타입용 enum class
     */
    enum class ShapeType {
        None, Circle, Rectangle
    }

    var brushSizes: LinkedList<Float> = LinkedList()        // 브러쉬 사이즈를 저장할 링크드리스트
    var brushPaths: LinkedList<Any> = LinkedList()          // 브러쉬의 Path 를 저장할 링크드리스트
    var brushColors: LinkedList<Int> = LinkedList()         // 브러쉬의 색깔을 저장할 링크드리스트
    var brushTypes: LinkedList<ShapeType> = LinkedList()    // 브러쉬의 도형 타입을 저장할 링크드리스트


    /**
     * 초기화 함수
     */
    fun init() {
        brushSizes.clear()
        brushPaths.clear()
        brushColors.clear()
        brushTypes.clear()
    }
}