package com.tr4nce.rabbittools.items.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;

public record WrenchGrid(Direction face, int column, int row) {
    private static final double[] DIVISIONS = {0.0, 0.25, 0.75, 1.0};
    private static final double FACE_OFFSET = 0.01;

    public static WrenchGrid at(BlockPos pos, Direction face, Vec3 location) {
        Vec3 local = location.subtract(pos.getX(), pos.getY(), pos.getZ());
        return new WrenchGrid(face, section(coordinate(horizontalAxis(face), local)),
                section(coordinate(verticalAxis(face), local)));
    }

    private static Axis horizontalAxis(Direction face) {
        return face.getAxis() == Axis.X ? Axis.Z : Axis.X;
    }

    private static Axis verticalAxis(Direction face) {
        return face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;
    }


    public Direction selectedDirection() {
        if (column == 1 && row == 1) return face;
        if (column != 1 && row != 1) return face.getOpposite();
        return column == 1 ? direction(verticalAxis(face), row) : direction(horizontalAxis(face), column);
    }

    public Vec3 point(int columnLine, int rowLine) {
        double alongU = DIVISIONS[columnLine];
        double alongV = DIVISIONS[rowLine];
        double offset = face.getAxisDirection() == AxisDirection.POSITIVE ? 1.0 + FACE_OFFSET : -FACE_OFFSET;
        return switch (face.getAxis()) {
            case X -> new Vec3(offset, alongV, alongU);
            case Y -> new Vec3(alongU, offset, alongV);
            case Z -> new Vec3(alongU, alongV, offset);
        };
    }

    private static Direction direction(Axis axis, int section) {
        return Direction.fromAxisAndDirection(axis, section == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
    }

    private static int section(double position) {
        if (position < DIVISIONS[1]) return 0;
        if (position > DIVISIONS[2]) return 2;
        return 1;
    }

    private static double coordinate(Axis axis, Vec3 point) {
        return axis.choose(point.x, point.y, point.z);
    }
}
