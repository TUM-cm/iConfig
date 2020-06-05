import math
import matplotlib.pyplot as plt
import numpy as np


def plot_simulation(wifix, wifiy, bluetoothx, bluetoothy, border, annotationwifi, annotationbluetooth, truedevices):

    angle = 0.5
    roomx = 6.82
    roomy = 10.2
    centerx = -0.37
    centery = -1.5
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
    wifix = [-0.900000006, -2.820000039, -2.890000029]
    wifiy = [-4.659999989, 0.770000061, -2.709999969]
    annotationwifi = ['b8:27:eb:2f:33:9d', 'b8:27:eb:37:08:29', 'b8:27:eb:34:ef:c6']
    bluetoothx = [-2.550000056, 1.140000028]
    bluetoothy = [-0.799999972, 1.430000072]
    annotationbluetooth = ['CC:26:77:B5:5A:BD', 'C9:ED:20:D2:6A:5F']
    border = [[-0.470000014, -5.44000001, 0.420000008, -5.070000005],
              [-2.640000036, -5.310000005, -0.470000014, -5.44000001],
              [-3.160000048, -4.969999997, -2.640000036, -5.310000005],
              [-3.700000075, -4.590000086, -3.160000048, -4.969999997],
              [-3.890000078, -4.340000083, -3.700000075, -4.590000086],
              [-4.370000085, -3.27000006, -3.890000078, -4.340000083],
              [-4.370000085, -2.68000005, -4.370000085, -3.27000006],
              [-4.370000085, -2.68000005, -4.329999992, -1.709999995],
              [-4.329999992, -1.709999995, -4.169999989, -0.629999971],
              [-4.169999989, -0.629999971, -4.00000007, 0.130000049],
              [-4.00000007, 0.130000049, -1.240000045, 2.420000097],
              [-1.240000045, 2.420000097, 0.569999991, 2.730000102],
              [0.569999991, 2.730000102, 1.810000018, 2.310000096], [1.810000018, 2.310000096, 2.24000003, 2.000000091],
              [2.24000003, 2.000000091, 2.350000032, 1.880000089], [2.350000032, 1.880000089, 2.390000033, 1.750000087],
              [2.390000033, 1.750000087, 2.930000026, -0.419999944],
              [2.930000026, -0.549999946, 2.930000026, -0.419999944],
              [0.420000008, -5.070000005, 2.930000026, -0.549999946]]

    truedevices = [[0.33,-3.3 , 'b8:27:eb:2f:33:9d'], [-2.67,1.6 , 'b8:27:eb:37:08:29'],  [-2.57,-4.8 , 'b8:27:eb:34:ef:c6'],  [-2.87,-2.4 , 'CC:26:77:B5:5A:BD'], [-0.77, 1.3, 'C9:ED:20:D2:6A:5F']]

    assert len(wifix) == len(wifiy)
    assert len(bluetoothx) == len(bluetoothy)


    plot_simulation(wifix, wifiy, bluetoothx, bluetoothy, border, annotationwifi, annotationbluetooth, truedevices)
    
if __name__ == "__main__":
    main()
