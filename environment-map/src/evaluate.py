import math
import matplotlib.pyplot as plt
import numpy as np


def plot_simulation(wifix, wifiy, bluetoothx, bluetoothy, border, annotationwifi, annotationbluetooth, truedevices):
    angle = 0.7
    roomx = 5.5
    roomy = 4.85
    centerx = -0.1
    centery = 0.6

    polygon_x = []
    polygon_y = []

    def rotate_around(datax, datay):
        resultx = []
        resulty = []
        for (pointx, pointy) in zip(datax, datay):
            resultx.append(pointx*math.cos(angle)-pointy*math.sin(angle))
            resulty.append(pointy*math.cos(angle)+pointx*math.sin(angle))
        return resultx, resulty

    def polygon_area(x, y):
        correction = x[-1] * y[0] - y[-1] * x[0]
        main_area = np.dot(x[:-1], y[1:]) - np.dot(y[:-1], x[1:])
        return 0.5 * np.abs(main_area + correction)

    def rotate_border(data):
        rotated_result = []
        for line in data:
            new_line = []
            x1 = line[0]
            y1 = line[1]
            x1_rotated = x1 * math.cos(angle) - y1 * math.sin(angle)
            y1_rotated = y1 * math.cos(angle) + x1 * math.sin(angle)
            new_line.append(x1_rotated)
            new_line.append(y1_rotated)
            x2 = line[2]
            y2 = line[3]
            x2_rotated = x2 * math.cos(angle) - y2 * math.sin(angle)
            y2_rotated = y2 * math.cos(angle) + x2 * math.sin(angle)
            new_line.append(x2_rotated)
            new_line.append(y2_rotated)

            rotated_result.append(new_line)
        return rotated_result

    def draw_rectangle(border):
        min_x = 999999
        max_x = -999999
        min_y = 999999
        max_y = -999999

        for line in border:
            x1 = line[0]
            y1 = line[1]
            x2 = line[2]
            y2 = line[3]

            if (x1 < min_x):
                min_x = x1
            if (x1 > max_x):
                max_x = x1
            if (y1 < min_y):
                min_y = y1
            if (y1 > max_y):
                max_y = y1

            if (x2 < min_x):
                min_x = x2
            if (x2 > max_x):
                max_x = x2
            if (y2 < min_y):
                min_y = y2
            if (y2 > max_y):
                max_y = y2

        plt.plot([min_x, max_x], [min_y, min_y], color='black')
        plt.plot([max_x, max_x], [min_y, max_y], color='black')
        plt.plot([min_x, max_x], [max_y, max_y], color='black')
        plt.plot([min_x, min_x], [max_y, min_y], color='black')

    def draw_real_boundary():
        min_x = centerx - (roomx / 2)
        max_x = centerx + (roomx / 2)
        min_y = centery - (roomy / 2)
        max_y = centery + (roomy / 2)

        plt.plot([min_x, max_x], [min_y, min_y], color='black', linestyle='-.')
        plt.plot([max_x, max_x], [min_y, max_y], color='black', linestyle='-.')
        plt.plot([min_x, max_x], [max_y, max_y], color='black', linestyle='-.')
        plt.plot([min_x, min_x], [max_y, min_y], color='black', linestyle='-.')

    def compute_evaluation(estimated_x, estimated_y, true_x, true_y):
        dx = abs(estimated_x - true_x)
        dy = abs(estimated_y - true_y)
        euclidean = math.sqrt(dx**2 + dy**2)
        delta_x.append(dx)
        delta_y.append(dy)
        list_euclidean.append(euclidean)

    delta_x = []
    delta_y = []
    list_euclidean = []
    wifix, wifiy = rotate_around(wifix, wifiy)
    bluetoothx, bluetoothy = rotate_around(bluetoothx, bluetoothy)
    border = rotate_border(border)


    fig, ax = plt.subplots()
    plt.gca().set_prop_cycle(None)

    for x, y, label in zip(wifix, wifiy, annotationwifi):
        plt.scatter(x, y, label=label)
        for device in truedevices:
            if label == device[2]:
                compute_evaluation(x, y, device[0], device[1])

    for i, txt in enumerate(annotationwifi):
        ax.annotate(txt, (wifix[i], wifiy[i]))

    for x, y, label in zip(bluetoothx, bluetoothy, annotationbluetooth):
        plt.scatter(x, y, label=label)
        for device in truedevices:
            if label == device[2]:
                compute_evaluation(x, y, device[0], device[1])

    for i, txt in enumerate(annotationbluetooth):
        ax.annotate(txt, (bluetoothx[i], bluetoothy[i]))

    plt.gca().set_prop_cycle(None)
    for device in truedevices:
        plt.scatter(device[0], device[1], marker="2", label=device[2])

    for l in border:
        x = [l[0], l[2]]
        y = [l[1], l[3]]
        polygon_x.append(l[0])
        polygon_x.append(l[2])
        polygon_y.append(l[1])
        polygon_y.append(l[3])
        plt.plot(x, y, color='black')

    print("real room area:")
    print(roomy*roomx)
    print("convex hull area:")
    print(polygon_area(polygon_x, polygon_y))
    draw_rectangle(border)
    draw_real_boundary()

    #compute mean and std
    mean_x = np.mean(delta_x)
    std_x = np.std(delta_x)
    mean_y = np.mean(delta_y)
    std_y = np.std(delta_y)
    mean_euclidean = np.mean(list_euclidean)
    std_euclidean = np.std(list_euclidean)


    print(" X Mean: ")
    print(mean_x)
    print(" X STD: ")
    print(std_x)
    print(" Y Mean: ")
    print(mean_y)
    print(" Y STD: ")
    print(std_y)
    print(" Euclidean Mean: ")
    print(mean_euclidean)
    print(" Euclidean STD: ")
    print(std_euclidean)


    ax.axis("equal")
    plt.show()
    plot_format="svg"
    fig.savefig("processedMap." + plot_format, format=plot_format, bbox_inches="tight", transparent=True)
    plt.close(fig)
    
def main():
    wifix = [-2.0200000099837774]
    wifiy = [-0.2600000105798238]
    annotationwifi = ['b8:27:eb:37:08:29']
    bluetoothx = [2.130000070482492]
    bluetoothy = [1.8200000390410396]
    annotationbluetooth = ['C9:ED:20:D2:6A:5F']
    border = [[3.080000071451071, -0.4280000168085102, 3.185000073015689, -0.37600001603364963],
              [2.723000063002111, -0.5880000191926961, 3.080000071451071, -0.4280000168085102],
              [1.491000037863848, -0.7000000208616272, 2.723000063002111, -0.5880000191926961],
              [-1.2320000303536662, -0.3120000067353259, 1.491000037863848, -0.7000000208616272],
              [-2.2610000608116407, 0.5600000154972098, -1.2320000303536662, -0.3120000067353259],
              [-2.2610000608116407, 0.5600000154972098, -2.2190000607073355, 0.7680000191926983],
              [-2.2190000607073355, 0.7680000191926983, -2.0790000586211717, 0.956000021994118],
              [-2.0790000586211717, 0.956000021994118, 0.6300000365078424, 2.552000049054623],
              [0.6300000365078424, 2.552000049054623, 2.3800000787526394, 1.668000034093858],
              [2.3800000787526394, 1.668000034093858, 3.4510000827163374, 0.6799999994039536],
              [3.4510000827163374, 0.6799999994039536, 3.5630000843852634, 0.5159999969601622],
              [3.5630000843852634, 0.3839999949932087, 3.5630000843852634, 0.5159999969601622],
              [3.185000073015689, -0.37600001603364963, 3.5630000843852634, 0.3839999949932087]]

    truedevices = [[0.7, 0.1, 'b8:27:eb:37:08:29'], [-1.8, 1, 'C9:ED:20:D2:6A:5F']]

    assert len(wifix) == len(wifiy)
    assert len(bluetoothx) == len(bluetoothy)


    plot_simulation(wifix, wifiy, bluetoothx, bluetoothy, border, annotationwifi, annotationbluetooth, truedevices)
    
if __name__ == "__main__":
    main()
