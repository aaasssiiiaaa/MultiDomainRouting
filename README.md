# MultiDomainRouting
###  拓扑
拓扑文件是根目录下的topo.json，读取到的信息中不包含边中波长的信息，其他的初始化都无误。
默认生成的域的拓扑是环形的，也就是单域域内所有点是一个环，整个多域把所有单域当成一个环串起来。生成环形拓扑的目的是先保证所有点之间都是通的。其次如果有需要可以再这个基础上加边生成mesh网
