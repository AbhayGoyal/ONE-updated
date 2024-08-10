#!/bin/bash
#-------------------------------------------------------------------------------
#  SBATCH CONFIG
#-------------------------------------------------------------------------------
## resources
#SBATCH --partition requeue
#SBATCH --nodes=1 #(please stick to single node for GPU experimentation)
#SBATCH --ntasks-per-node=128
#SBATCH --cpus-per-task=1
#SBATCH --mem 0G
#SBATCH --time 48:00:00
#SBATCH --job-name=CustomRouter_2_3_14400_dense_30M_transmit_100_512 
##SBATCH --gres=gpu:A100:1
##SBATCH --gres=gpu:1g.10gb:4
##SBATCH --gres=cpu
#SBATCH --output=CustomRouter_2_3_14400_dense_30M_transmit_100_512-%x.%j.out
#SBATCH --error=CustomRouter_2_3_14400_dense_30M_transmit_100_512-%x.%j.err

module load jdk
module load miniconda3

source activate myWork

sh compile.sh

# 
python3 /home/aghnw/data/miniconda/envs/myWork/myWork/flask_try.py & sh one.sh -b 10