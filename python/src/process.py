import cv2
from os import path
import numpy as np
import math


ASSETS_DIR = path.join(path.dirname(path.realpath(__file__)), '../assets')


def sliding_window(image, stepSize, windowSize):
    # slide a window across the image
    for y in range(0, image.shape[0], stepSize):
        for x in range(0, image.shape[1], stepSize):
            # yield the current window
            yield (x, y, image[y:y + windowSize[1], x:x + windowSize[0]])


def main():
    stations = cv2.imread(path.join(ASSETS_DIR, 'stations.png'))

    # threshold for blue
    blue = stations[:,:,2]
    _, blue = cv2.threshold(blue, 20, 255, cv2.THRESH_BINARY)
    blue = cv2.morphologyEx(blue, cv2.MORPH_CLOSE, np.ones((7, 7)))
    _, contours, _ = cv2.findContours(blue, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    points = []
    for contour in contours:
        M = cv2.moments(contour)
        x = int(M['m10'] / M['m00'])
        y = int(M['m01'] / M['m00'])
        points.append((x, y))

    # Note: this doesn't quite work sooo I guess we're going with manual translation
    # something something numerical instability

    # # reference points
    # LEFT_LATLON = (40.341910, -74.660938)
    # # RIGHT_LATLON = (40.342308, -74.656715)
    # TOP_LATLON = (40.350204, -74.656573)
    # BOTTOM_LATLON = (40.342307, -74.656715)
    #
    # points.sort(key=lambda p: p[0])
    # left_pt = points[0]
    # right_pt = points[-1]
    #
    # points.sort(key=lambda p: p[1])
    # top_pt = points[0]
    # bot_pt = points[-1]
    #
    # # find homography
    # src_pts = np.float64((left_pt, top_pt, bot_pt)).reshape(-1, 1, 2)
    # dst_pts = np.float64((LEFT_LATLON, TOP_LATLON, BOTTOM_LATLON)).reshape(-1, 1, 2)
    # M, _ = cv2.findHomography(src_pts, dst_pts)
    #
    # # project the points
    # pts = np.float64(points).reshape(-1, 1, 2)
    # latlon = cv2.perspectiveTransform(pts, M)
    # print(latlon)

    # scale by 2.588888889054482e-07, 7.050230125517975e-06
    # rotate CCW by 0.350763562 rad
    # translate by 40.34188903 -74.66430801

    # the three bottom points are false positives
    for x, y in points[:-3]:
        # sx = 2.588888889054482e-07 * x
        # sy = 7.050230125517975e-06 * y
        # latlon.append((sx * math.cos(0.350763562) - sy * math.sin(0.350763562) + 40.34188903,
        #                sx * math.sin(0.350763562) + sy * math.cos(0.350763562) - 74.66430801))
        cv2.circle(stations, (x, y), 4, (0, 255, 0), cv2.FILLED)

    cv2.imwrite(path.join(ASSETS_DIR, 'out.png'), stations)


if __name__ == '__main__':
    main()