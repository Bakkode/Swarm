


/**
*   fromX   = {fx1, fx2, ..}
*   fromY   = {fy1, fy2, ..}
*   toX     = {tx1, tx2, ..}
*   toY     = {ty1, ty2, ..}
*/
kernel computeIntersect(Val fromX, Val fromY, Val toX, Val toY){
    Val dx = vec_sub(fromX, toX);
    Val dy = vec_sub(fromY, toY);

    Val zeroing = cmp_neq(dx, 0.0);

    Val gr = vec_div(dy, dx);

    Val c0 = vec_mul(fromX, gr);
    Val c1 = vec_sub(fromY, c0);
    Val c  = vec_mul(zeroing, c1);

    Val[] rdx = split(dx, 2, 2);
    Val[] rx  = split(fromX, 2, 2);
    Val[] rgr = split(gr, 2, 2);
    Val[] rc  = split(c, 2, 2);

    Val fmul = vec_mul(rdx[0], rdx[1]);

    Val retX0 = vec_mul(rx, rZeroing);
    Val retX1 = vec_mul(rx, rZeroing);
}


__gpu__
void intersect(float* result, int length, float* fromPoint1, float* toPoint1, float* fromPoint2, float* toPoint2) {
    index(x, length) {
 
        int xIndex = x * 2;
        int yIndex = xIndex + 1;

        float x1 = toPoint1[xIndex];
        float x2 = toPoint2[xIndex];
        float y1 = toPoint1[yIndex];
        float y2 = toPoint2[yIndex];

        float dx1 = fromPoint1[xIndex] - x1;
        float dx2 = fromPoint2[xIndex] - x2;
        float dy1 = fromPoint1[yIndex] - y1;
        float dy2 = fromPoint2[yIndex] - y2;

        float zeroing1 = (dx1 != 0.0);
        float zeroing2 = (dx2 != 0.0);

        float gr1 = dy1 / dx1;
        float gr2 = dy2 / dx2;      

        float c1  = zeroing1 * (y1 - (x1 * gr1));
        float c2  = zeroing2 * (y2 - (x2 * gr2));

        if (dx1 * dx2 == 0.0) {
            result[xIndex] = (x1 * zeroing1) + (x2 * zeroing2);
            result[yIndex] = result[xIndex] * (gr1 + gr2) + c1 + c2;
            return;
        }

        // +8
        float dv = gr2 - gr1;
        result[xIndex] = (c1 - c2) / dv;
        result[yIndex] = ((c1 * gr2) - (c2 * gr1)) / dv;
    }
}